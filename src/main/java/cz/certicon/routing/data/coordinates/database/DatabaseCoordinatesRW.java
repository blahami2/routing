/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.database;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractDatabase;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DatabaseCoordinatesRW extends AbstractDatabase<Map<Edge, List<Coordinate>>, Set<Edge>> implements CoordinateReader, CoordinateWriter {

    public DatabaseCoordinatesRW( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Map<Edge, List<Coordinate>> checkedRead( Set<Edge> edges ) throws SQLException {
        Map<Edge, List<Coordinate>> coordinateMap = new HashMap<>();
        Map<Edge.Id, Edge> edgeMap = new HashMap<>();
        for ( Edge edge : edges ) {
            edgeMap.put( edge.getId(), edge );
        }
        ResultSet rs;
        StringBuilder inArray = new StringBuilder();
        for ( Edge edge : edges ) {
            inArray.append( edge.getId().getValue() ).append( ", " );
        }
        if ( edges.size() > 0 ) {
            inArray.delete( inArray.length() - 2, inArray.length() );
        }
        rs = getStatement().executeQuery( "SELECT e.id, ST_AsText(d.geom) AS linestring, d.source_lat, d.source_lon, d.target_lat, d.target_lon "
                + "FROM edges_routing e "
                + "JOIN edges_data_routing d ON e.data_id = d.id "
                + "WHERE e.id IN ("
                + inArray.toString()
                + ")" );
        int idColumnIdx = rs.findColumn( "id" );
        int linestringColumnIdx = rs.findColumn( "linestring" );
        int sourceLonColumnIdx = rs.findColumn( "source_lon" );
        int sourceLatColumnIdx = rs.findColumn( "source_lat" );
        int targetLonColumnIdx = rs.findColumn( "target_lon" );
        int targetLatColumnIdx = rs.findColumn( "target_lat" );
        while ( rs.next() ) {
            Coordinate sourceCoord = parseCoord( rs, sourceLatColumnIdx, sourceLonColumnIdx );
            Coordinate targetCoord = parseCoord( rs, sourceLatColumnIdx, sourceLonColumnIdx );
            List<Coordinate> coordinates = new ArrayList<>();
            String linestring = rs.getString( linestringColumnIdx );
            linestring = linestring.substring( "LINESTRING(".length(), linestring.length() - ")".length() );
            for ( String cord : linestring.split( "," ) ) {
                Coordinate coord = new Coordinate(
                        Double.parseDouble( cord.split( " " )[1] ),
                        Double.parseDouble( cord.split( " " )[0] )
                );
                coordinates.add( coord );
            }
            Edge edge = edgeMap.get( Edge.Id.createId( rs.getLong( idColumnIdx ) ) );
            Node sourceNode = edge.getSourceNode();
            Node targetNode = edge.getTargetNode();
            if ( sourceCoord.equals( sourceNode.getCoordinates() ) ) {
            } else if ( sourceCoord.equals( targetNode.getCoordinates() ) ) {
                Collections.reverse( coordinates );
            } else {
                // test target coord?
                System.out.println( "edge id = " + rs.getLong( idColumnIdx ) );
                System.out.println( "source coord = " + sourceCoord );
                System.out.println( "target coord = " + targetCoord );
                System.out.println( "node source coord = " + sourceNode.getCoordinates() );
                System.out.println( "node target coord = " + targetNode.getCoordinates() );
                throw new IllegalArgumentException( "Edge and it's coordinates do not match." );
            }
            coordinateMap.put( edge, coordinates );
        }
        return coordinateMap;
    }

    @Override
    protected void checkedWrite( Map<Edge, List<Coordinate>> in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    private Coordinate parseCoord( ResultSet rs, int latColumnIndex, int lonColumnIndex ) throws SQLException {
        int lat = rs.getInt( latColumnIndex );
        int lon = rs.getInt( lonColumnIndex );
        return new Coordinate(
                (double) lat * 10E-8, (double) lon * 10E-8 );
    }
}