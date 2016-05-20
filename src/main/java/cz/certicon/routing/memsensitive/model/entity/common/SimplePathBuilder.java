/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.Path;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.model.entity.Coordinates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimplePathBuilder implements PathBuilder<Path, Graph> {

    private final ArrayList<Coordinates> coordinates = new ArrayList<>();
    private double time = 0;
    private double length = 0;

    public SimplePathBuilder() {
    }

    @Override
    public void addEdge( Graph graph, long edgeId, boolean isForward, List<Coordinates> edgeCoordinates, double length, double time ) {
        if ( !isForward ) {
            Collections.reverse( edgeCoordinates );
        }
        this.coordinates.addAll( edgeCoordinates );
        addLength( length );
        addTime( time );
    }

    @Override
    public void addCoordinates( Coordinates coords ) {
        this.coordinates.add( coords );
    }

    @Override
    public void addLength( double length ) {
        this.length += length;
    }

    @Override
    public void addTime( double time ) {
        this.time += time;
    }

    @Override
    public Path build() {
        return new SimplePath( coordinates, length, time );
    }

}
