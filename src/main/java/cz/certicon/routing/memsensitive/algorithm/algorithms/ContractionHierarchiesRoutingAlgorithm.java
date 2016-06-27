/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.algorithms;

import static cz.certicon.routing.GlobalOptions.MEASURE_STATS;
import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private static final double ARRAY_COPY_RATIO = 0.01;

    private final Graph graph;
    private final int[] nodeFromPredecessorArray;
    private final float[] nodeFromDistanceArray;
    private final BitArray nodeFromClosedArray;
    private final NodeDataStructure<Integer> nodeFromDataStructure;
    private final int[] nodeToPredecessorArray;
    private final float[] nodeToDistanceArray;
    private final BitArray nodeToClosedArray;
    private final NodeDataStructure<Integer> nodeToDataStructure;
    private final PreprocessedData preprocessedData;

    public ContractionHierarchiesRoutingAlgorithm( Graph graph, PreprocessedData preprocessedData ) {
        this.graph = graph;
        this.nodeFromPredecessorArray = new int[graph.getNodeCount()];
        this.nodeFromDistanceArray = new float[graph.getNodeCount()];
        this.nodeFromClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeFromDataStructure = new JgraphtFibonacciDataStructure();
        this.nodeToPredecessorArray = new int[graph.getNodeCount()];
        this.nodeToDistanceArray = new float[graph.getNodeCount()];
        this.nodeToClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeToDataStructure = new JgraphtFibonacciDataStructure();
        this.preprocessedData = preprocessedData;

        graph.resetNodeDistanceArray( nodeFromDistanceArray );
        graph.resetNodePredecessorArray( nodeFromPredecessorArray );
        graph.resetNodeClosedArray( nodeFromClosedArray );
        graph.resetNodeDistanceArray( nodeToDistanceArray );
        graph.resetNodePredecessorArray( nodeToPredecessorArray );
        graph.resetNodeClosedArray( nodeToClosedArray );
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException {
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        TIntList nodesFromVisited = new TIntArrayList();
        TIntList nodesToVisited = new TIntArrayList();

        for ( NodeEntry nodeEntry : from.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            nodeFromDistanceArray[node] = distance;
            nodeFromDataStructure.add( node, distance );
            nodeFromPredecessorArray[node] = edge;
        }
        for ( NodeEntry nodeEntry : to.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            nodeToDistanceArray[node] = distance;
            nodeToDataStructure.add( node, distance );
            nodeToPredecessorArray[node] = edge;
        }
        while ( !nodeFromDataStructure.isEmpty() || !nodeToDataStructure.isEmpty() ) {
            if ( !nodeFromDataStructure.isEmpty() ) {
                int currentNode = nodeFromDataStructure.extractMin();
//                System.out.println( "F: extracted: " + currentNode );
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeFromClosedArray.set( currentNode, true );
                nodesFromVisited.add( currentNode );
                int sourceRank = preprocessedData.getRank( currentNode );
                float currentDistance = nodeFromDistanceArray[currentNode];
//                System.out.println( "F: distance = " + currentDistance );
                TIntIterator outgoingEdgesIterator = preprocessedData.getOutgoingEdgesIterator( currentNode, graph );
                while ( outgoingEdgesIterator.hasNext() ) {
                    int edge = outgoingEdgesIterator.next();
                    // TODO
//                    if ( !graph.isValidWay( currentNode, edge, nodeFromPredecessorArray ) ) {
//                        continue;
//                    }
                    int otherNode = preprocessedData.getOtherNode( edge, currentNode, graph );
                    if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                        if ( MEASURE_STATS ) {
                            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                        }
                        float otherNodeDistance = nodeFromDistanceArray[otherNode];
                        float distance = currentDistance + preprocessedData.getLength( edge, graph );
                        if ( distance < otherNodeDistance ) {
                            nodeFromDistanceArray[otherNode] = distance;
//                                System.out.println( "F: pred for " + otherNode + " = " + edge );
                            nodeFromPredecessorArray[otherNode] = edge;
                            nodeFromDataStructure.notifyDataChange( otherNode, distance );
                        }
                    }
                }
            }
            if ( !nodeToDataStructure.isEmpty() ) {
                int currentNode = nodeToDataStructure.extractMin();
//                System.out.println( "T: extracted: " + currentNode );
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeToClosedArray.set( currentNode, true );
                int sourceRank = preprocessedData.getRank( currentNode );
                float currentDistance = nodeToDistanceArray[currentNode];
                nodesToVisited.add( currentNode );
//                System.out.println( "T: distance = " + currentDistance );

                TIntIterator incomingEdgesIterator = preprocessedData.getIncomingEdgesIterator( currentNode, graph );
                while ( incomingEdgesIterator.hasNext() ) {
                    int edge = incomingEdgesIterator.next();
                    // TODO
//                    if ( !graph.isValidWay( currentNode, edge, nodeToPredecessorArray ) ) {
//                        continue;
//                    }
                    int otherNode = preprocessedData.getOtherNode( edge, currentNode, graph );
                    if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                        if ( MEASURE_STATS ) {
                            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                        }
                        float otherNodeDistance = nodeToDistanceArray[otherNode];
                        float distance = currentDistance + preprocessedData.getLength( edge, graph );
                        if ( distance < otherNodeDistance ) {
                            nodeToDistanceArray[otherNode] = distance;
//                                System.out.println( "F: pred for " + otherNode + " = " + edge );
                            nodeToPredecessorArray[otherNode] = edge;
                            nodeToDataStructure.notifyDataChange( otherNode, distance );
                        }
                    }
                }
            }
        }
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        TIntIterator itFrom = nodesFromVisited.iterator();
        while ( itFrom.hasNext() ) {
            int node = itFrom.next();
            if ( nodeFromClosedArray.get( node ) && nodeToClosedArray.get( node ) ) {
                double distance = nodeFromDistanceArray[node] + nodeToDistanceArray[node];
                if ( 0 <= distance && distance < finalDistance ) {
                    finalDistance = distance;
                    finalNode = node;
                }
            }
        }

        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.PAUSE );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.START );
        }
        if ( finalNode != -1 ) {
//            System.out.println( "final node = " + finalNode );
            // set target to final, then add as first, then add as last for the "to" dijkstra
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalNode ) );
            int pred = nodeFromPredecessorArray[finalNode];
            int currentNode = finalNode;
            while ( graph.isValidPredecessor( pred ) ) {
//                System.out.println( "F: pred = " + pred );
                int node = addEdgeAsFirst( routeBuilder, pred, currentNode );
//                System.out.println( "F: node = " + node );
                pred = nodeFromPredecessorArray[node];
                currentNode = node;
                NodeEntry nodeEntry = from.get( node );
                if ( nodeEntry != null && nodeEntry.getNodeId() == node && nodeEntry.getEdgeId() == pred ) { // omit the first edge
                    break;
                }
            }
            currentNode = finalNode;
            pred = nodeToPredecessorArray[finalNode];
            while ( graph.isValidPredecessor( pred ) ) {
//                System.out.println( "T: pred = " + pred );
                int node = addEdgeAsLast( routeBuilder, pred, currentNode );
//                System.out.println( "T: node = " + node );
                pred = nodeToPredecessorArray[node];
                currentNode = node;
                NodeEntry nodeEntry = to.get( node);
                if(nodeEntry != null && nodeEntry.getNodeId() == node && nodeEntry.getEdgeId() == pred){ // omit the last edge
                    break;
                }
            }
        } else {
            throw new RouteNotFoundException();
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }

        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.CONTINUE );
        }
        if ( nodesFromVisited.size() < graph.getNodeCount() * ARRAY_COPY_RATIO ) {
            TIntIterator it = nodesFromVisited.iterator();
            while ( it.hasNext() ) {
                int node = it.next();
                nodeFromDistanceArray[node] = Graph.DISTANCE_DEFAULT;
                nodeFromPredecessorArray[node] = Graph.PREDECESSOR_DEFAULT;
                nodeFromClosedArray.set( node, Graph.CLOSED_DEFAULT );
            }
        } else {
            graph.resetNodeDistanceArray( nodeFromDistanceArray );
            graph.resetNodePredecessorArray( nodeFromPredecessorArray );
            graph.resetNodeClosedArray( nodeFromClosedArray );
        }
        if ( nodesToVisited.size() < graph.getNodeCount() * ARRAY_COPY_RATIO ) {
            TIntIterator it = nodesToVisited.iterator();
            while ( it.hasNext() ) {
                int node = it.next();
                nodeToDistanceArray[node] = Graph.DISTANCE_DEFAULT;
                nodeToPredecessorArray[node] = Graph.PREDECESSOR_DEFAULT;
                nodeToClosedArray.set( node, Graph.CLOSED_DEFAULT );
            }
        } else {
            graph.resetNodeDistanceArray( nodeToDistanceArray );
            graph.resetNodePredecessorArray( nodeToPredecessorArray );
            graph.resetNodeClosedArray( nodeToClosedArray );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.STOP );
        }

        return routeBuilder.build();
    }

    private <R> int addEdgeAsFirst( RouteBuilder<R, Graph> routeBuilder, int edge, int currentNode ) {
        if ( edge < graph.getEdgeCount() ) { // edge
            routeBuilder.addEdgeAsFirst( graph, graph.getEdgeOrigId( edge ) );
            return graph.getOtherNode( edge, currentNode );
        } else { // shortcut
            edge -= graph.getEdgeCount();
            addEdgeAsFirst( routeBuilder, preprocessedData.getEndEdge( edge ), currentNode );
            addEdgeAsFirst( routeBuilder, preprocessedData.getStartEdge( edge ), currentNode );
            return preprocessedData.getSource( edge );
        }
    }

    private <R> int addEdgeAsLast( RouteBuilder<R, Graph> routeBuilder, int edge, int currentNode ) {
        if ( edge < graph.getEdgeCount() ) { // edge
//            System.out.println( "edge: " + edge );
            routeBuilder.addEdgeAsLast( graph, graph.getEdgeOrigId( edge ) );
            return graph.getOtherNode( edge, currentNode );
        } else { // shortcut
            edge -= graph.getEdgeCount();
//            System.out.println( "shortcut: " + edge );
            addEdgeAsLast( routeBuilder, preprocessedData.getStartEdge( edge ), currentNode );
            addEdgeAsLast( routeBuilder, preprocessedData.getEndEdge( edge ), currentNode );
            return preprocessedData.getTarget( edge );
        }
    }
}
