/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.ch;

import cz.certicon.routing.memsensitive.algorithm.algorithms.ContractionHierarchiesRoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.containers.IntArrayContainer;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.utils.EffectiveUtils;
import gnu.trove.iterator.TIntIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class PreprocessedData {

    private final int[] ranks;
    private final int[][] incomingShortcuts;
    private final int[][] outgoingShortcuts;
    private final int[] sources;
    private final int[] targets;
    private int[][][] turnRestrictions;

    /* more memory, more efficiency */
    private final int[] startEdges;
    private final int[] endEdges;

    private final int[] startOrigEdges;
    private final int[] endOrigEdges;

    private final float[] lengths;

    private final long startId;
    private DistanceType distanceType;

    public PreprocessedData( int nodeCount, int edgeCount, int shortcutCount, long startId ) {
        this.ranks = new int[nodeCount];
        this.incomingShortcuts = new int[nodeCount][];
        this.outgoingShortcuts = new int[nodeCount][];
        this.sources = new int[shortcutCount];
        this.targets = new int[shortcutCount];

        this.startEdges = new int[shortcutCount];
        this.endEdges = new int[shortcutCount];
        this.startOrigEdges = new int[shortcutCount];
        this.endOrigEdges = new int[shortcutCount];
        this.lengths = new float[shortcutCount];
        EffectiveUtils.fillArray( lengths, -1 );
        this.startId = startId;
    }

    public PreprocessedData( int[] ranks, int[][] incomingShortcuts, int[][] outgoingShortcuts, int[] sources, int[] targets, int[] startEdges, int[] endEdges, int[] startOrigEdges, int[] endOrigEdges, long startId ) {
        this.ranks = ranks;
        this.incomingShortcuts = incomingShortcuts;
        this.outgoingShortcuts = outgoingShortcuts;
        this.sources = sources;
        this.targets = targets;
        this.startEdges = startEdges;
        this.endEdges = endEdges;
        this.startOrigEdges = startOrigEdges;
        this.endOrigEdges = endOrigEdges;
        this.lengths = new float[sources.length];
        EffectiveUtils.fillArray( lengths, -1 );
        this.startId = startId;
    }

    public void setRank( int node, int rank ) {
        ranks[node] = rank;
    }

    public void setIncomingShortcuts( int node, int[] incomingShortcuts ) {
        this.incomingShortcuts[node] = incomingShortcuts;
    }

    public void setOutgoingShortcuts( int node, int[] outgoingShortcuts ) {
        this.outgoingShortcuts[node] = outgoingShortcuts;
    }

    public void setShortcutSource( int shortcut, int node ) {
        sources[shortcut] = node;
    }

    public void setShortcutTarget( int shortcut, int node ) {
        targets[shortcut] = node;
    }

    public void setStartEdge( int shortcut, int edge ) {
        startEdges[shortcut] = edge;
    }

    public void setEndEdge( int shorcut, int edge ) {
        endEdges[shorcut] = edge;
    }

    public int[] getRanks() {
        return ranks;
    }

    public int getRank( int rank ) {
        return ranks[rank];
    }

    public int[][] getIncomingShortcuts() {
        return incomingShortcuts;
    }

    public int[] getIncomingShortcuts( int node ) {
        return incomingShortcuts[node];
    }

    public int[][] getOutgoingShortcuts() {
        return outgoingShortcuts;
    }

    public int[] getOutgoingShortcuts( int node ) {
        return outgoingShortcuts[node];
    }

    public int[] getSources() {
        return sources;
    }

    public int getShortcutCount() {
        return sources.length;
    }

    public int getSource( int shortcut ) {
        return sources[shortcut];
    }

    public void setDistanceType( DistanceType distanceType ) {
        this.distanceType = distanceType;
    }

    public DistanceType getDistanceType() {
        return distanceType;
    }

    public int getSource( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getSource( edge );
        }
        return sources[edge - graph.getEdgeCount()];
    }

    public int[] getTargets() {
        return targets;
    }

    public int getTarget( int shortcut ) {
        return targets[shortcut];
    }

    public int getTarget( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getTarget( edge );
        }
        return targets[edge - graph.getEdgeCount()];
    }

    public int getOtherNode( int edge, int node, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getOtherNode( edge, node );
        }
        edge -= graph.getEdgeCount();
        if ( targets[edge] != node ) {
            return targets[edge];
        }
        return sources[edge];
    }

    public int getStartEdge( int shortcut ) {
        return startEdges[shortcut];
    }

    public int getEndEdge( int shortcut ) {
        return endEdges[shortcut];
    }

    private int getOrigStartEdge( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return edge;
        } else {
            return startOrigEdges[edge - graph.getEdgeCount()];
        }
    }

    private int getOrigEndEdge( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return edge;
        } else {
            return endOrigEdges[edge - graph.getEdgeCount()];
        }
    }

    public long getStartId() {
        return startId;
    }

    public int getEdgeByOrigId( long edgeId, Graph graph ) {
        try {
            return graph.getEdgeByOrigId( edgeId );
        } catch ( NullPointerException ex ) {
            // does not contain
            return (int) ( edgeId - startId + graph.getEdgeCount() );
        }
    }

    public long getEdgeOrigId( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getEdgeOrigId( edge );
        }
        return edge - graph.getEdgeCount() + startId;
    }

    public float getLength( int shortcut, Graph graph ) {
        if ( shortcut < graph.getEdgeCount() ) {
            return graph.getLength( shortcut );
        }
        shortcut -= graph.getEdgeCount();
        if ( lengths[shortcut] < 0 ) {
            int start = startEdges[shortcut];
            int end = endEdges[shortcut];
            lengths[shortcut] = getLength( start, graph ) + getLength( end, graph );
        }
        return lengths[shortcut];
    }

    public boolean hasRestriction( int node, Graph graph ) {
        return graph.hasRestriction( node ) || ( turnRestrictions != null && turnRestrictions[node] != null );
    }

    public void setTurnRestrictions( int[][][] turnRestrictions ) {
//        // TTDEBUG
//        if ( turnRestrictions != null ) {
//            int maxLen = 0;
//            for ( int i = 0; i < turnRestrictions.length; i++ ) {
//                if ( turnRestrictions[i] != null ) {
//                    maxLen = Math.max( maxLen, turnRestrictions[i].length );
//                }
//            }
//            System.out.println( getClass().getSimpleName() + "-maxlen = " + maxLen );
//        } else {
//            System.out.println( getClass().getSimpleName() + "-null " );
//        }
        this.turnRestrictions = turnRestrictions;
    }

    public int[][][] getTurnRestrictions() {
        return turnRestrictions;
    }

    public Iterator<IntArrayContainer> getTurnTableSequencesIterator( int node, int edge, Graph graph ) {
        return new TurnTableSequenceIterator( graph.getTurnRestrictions(), turnRestrictions, node );

    }

    public boolean isValidWay( NodeState state, int targetEdge, Map<NodeState, NodeState> predecessorArray, Graph graph ) {
        int se = getOrigEndEdge( state.getEdge(), graph );
        int te = getOrigStartEdge( targetEdge, graph );
        if ( state.getEdge() >= 0 && ( graph.getOtherNode( se, state.getNode() ) == graph.getOtherNode( te, state.getNode() ) ) ) {
            return false;
        }

        if ( turnRestrictions == null ) { // without turn restrictions, everything is valid
            return state.getEdge() >= graph.getEdgeCount() || graph.isValidWay( state, targetEdge, predecessorArray );
        }
        int node = state.getNode();
        if ( turnRestrictions[node] == null ) { // without turn restrictions for the concrete node, every turn is valid
            return state.getEdge() >= graph.getEdgeCount() || graph.isValidWay( state, targetEdge, predecessorArray );
        }
        for ( int i = 0; i < turnRestrictions[node].length; i++ ) { // for all restrictions for this node
            int[] edgeSequence = turnRestrictions[node][i]; // load the edge sequence of this particular restrictions
            if ( edgeSequence[edgeSequence.length - 1] == targetEdge ) { // if the last edge of this sequence is the target edge
                NodeState currState = state;
                for ( int j = edgeSequence.length - 2; j >= 0 && currState != null; j-- ) { // for every edge in the sequence (except for the last, it is already checked) compare it with the predecessor
                    if ( currState.getEdge() != edgeSequence[j] ) {
                        break;
                    }
                    if ( j == 0 ) { // all passed, the turn restriction edge sequence matches the way, therefore it is forbidden
//                        System.out.println( "#" + getNodeOrigId( node ) + ": matches: " );
//                        String predE = "" + targetEdge;
//                        NodeState s = state;
//                        while ( s != null && s.getEdge() >= 0 ) {
//                            predE = getEdgeOrigId( s.getEdge() ) + " " + predE;
//                            s = predecessorArray.get( s );
//                        }
//                        System.out.println( "current path: " + predE );
//                        String tr = "";
//                        for ( int e : edgeSequence ) {
//                            tr = tr + getEdgeOrigId( e ) + " ";
//                        }
//                        System.out.println( "turn restriction way: " + tr );
                        return false;
                    }
                    currState = predecessorArray.get( currState );
                }
            }
        }
        return state.getEdge() >= graph.getEdgeCount() || graph.isValidWay( state, targetEdge, predecessorArray );
    }

    public boolean isValidWay( LinkedList<ContractionHierarchiesRoutingAlgorithm.TurnTableSequenceOpposite> currentTurnTables, int targetEdge ) {
        for ( ContractionHierarchiesRoutingAlgorithm.TurnTableSequenceOpposite currentTurnTable : currentTurnTables ) {
            if ( currentTurnTable.current == 1 // the next will be the last edge
                    && currentTurnTable.edges[0] == targetEdge ) { // and the last edge matches the target edge
                return false;
            }
        }
        return true;
    }

    public boolean isValidWay( NodeState state, LinkedList<ContractionHierarchiesRoutingAlgorithm.TurnTableSequenceOpposite> currentTurnTables, Map<NodeState, NodeState> predecessorArray, Graph graph ) {
//        System.out.println( "checking: #" + graph.getNodeOrigId( state.getNode() ) + "-edge#" + ( state.getEdge() >= 0 ? getEdgeOrigId( state.getEdge(), graph ) : -1 ) );
        for ( ContractionHierarchiesRoutingAlgorithm.TurnTableSequenceOpposite currentTurnTable : currentTurnTables ) {
            NodeState currentState = state;
            for ( int i = currentTurnTable.current - 1; i >= 0; i++ ) {
                int edge = currentTurnTable.edges[i];
//                System.out.print( ( currentState.getEdge() >= 0 ? getEdgeOrigId( currentState.getEdge(), graph ) : -1 ) + " =? " + ( edge >= 0 ? getEdgeOrigId( edge, graph ) : -1 ) );
                if ( currentState.getEdge() < 0 || currentState.getEdge() != edge ) { // if the path ends or the edges do not match
                    break;
                }
                if ( i == 0 ) { // all match
//                    System.out.println( "-match" );
                    return false;
                }
                currentState = predecessorArray.get( currentState );
            }
//            System.out.println( "-ok" );
        }
//        System.out.println( "-all-ok" );
        return true;
    }

    public TIntIterator getIncomingEdgesIterator( int node, Graph graph ) {
        return new IncomingIterator( graph, node );
    }

    public TIntIterator getOutgoingEdgesIterator( int node, Graph graph ) {
        return new OutgoingIterator( graph, node );
    }

    private class IncomingIterator implements TIntIterator {

        private final int node;
        private final Graph graph;
        private int position = -1;

        public IncomingIterator( Graph graph, int node ) {
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() { // ... see note at NeighbourListGraph
            return position + 1 < graph.getIncomingEdges( node ).length + incomingShortcuts[node].length;
        }

        @Override
        public int next() {
            if ( position + 1 < graph.getIncomingEdges( node ).length ) {
                return graph.getIncomingEdges( node )[++position];
            } else {
                return incomingShortcuts[node][++position - graph.getIncomingEdges( node ).length] + graph.getEdgeCount();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private class OutgoingIterator implements TIntIterator {

        private final int node;
        private int position = -1;
        private final Graph graph;

        public OutgoingIterator( Graph graph, int node ) {
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() { // see above, analogically
            return position + 1 < graph.getOutgoingEdges( node ).length + outgoingShortcuts[node].length;
        }

        @Override
        public int next() {
            if ( position + 1 < graph.getOutgoingEdges( node ).length ) {
                return graph.getOutgoingEdges( node )[++position];
            } else {
                return outgoingShortcuts[node][++position - graph.getOutgoingEdges( node ).length] + graph.getEdgeCount();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class TurnTableSequenceIterator implements Iterator<IntArrayContainer> {

        private final int[][][] graphTts;
        private final int[][][] dataTts;
        private final int node;
        private int counter = -1;
        private final int graphLen;
        private final int dataLen;

        public TurnTableSequenceIterator( int[][][] graphTts, int[][][] dataTts, int node ) {
            this.graphTts = graphTts;
            this.dataTts = dataTts;
            this.node = node;
            this.graphLen = ( graphTts == null || graphTts[node] == null ) ? 0 : graphTts[node].length;
            this.dataLen = ( dataTts == null || dataTts[node] == null ) ? 0 : dataTts[node].length;
        }

        @Override
        public boolean hasNext() {
            return counter + 1 < graphLen + dataLen;
        }

        @Override
        public IntArrayContainer next() {
            if ( counter + 1 < graphTts[node].length ) {
                return new IntArrayContainer( graphTts[node][++counter] );
            } else {
                return new IntArrayContainer( dataTts[node][++counter - graphLen] );
            }
        }

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.hashCode( this.ranks );
        hash = 53 * hash + Arrays.deepHashCode( this.incomingShortcuts );
        hash = 53 * hash + Arrays.deepHashCode( this.outgoingShortcuts );
        hash = 53 * hash + Arrays.hashCode( this.sources );
        hash = 53 * hash + Arrays.hashCode( this.targets );
        hash = 53 * hash + Arrays.hashCode( this.startEdges );
        hash = 53 * hash + Arrays.hashCode( this.endEdges );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PreprocessedData other = (PreprocessedData) obj;
        if ( !Arrays.equals( this.ranks, other.ranks ) ) {
            return false;
        }
        if ( !Arrays.deepEquals( this.incomingShortcuts, other.incomingShortcuts ) ) {
            return false;
        }
        if ( !Arrays.deepEquals( this.outgoingShortcuts, other.outgoingShortcuts ) ) {
            return false;
        }
        if ( !Arrays.equals( this.sources, other.sources ) ) {
            return false;
        }
        if ( !Arrays.equals( this.targets, other.targets ) ) {
            return false;
        }
        if ( !Arrays.equals( this.startEdges, other.startEdges ) ) {
            return false;
        }
        if ( !Arrays.equals( this.endEdges, other.endEdges ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder isb = new StringBuilder();
        isb.append( "[" );
        for ( int[] i : incomingShortcuts ) {
            isb.append( Arrays.toString( i ) ).append( "," );
        }
        isb.append( "]" );
        StringBuilder osb = new StringBuilder();
        osb.append( "[" );
        for ( int[] i : outgoingShortcuts ) {
            osb.append( Arrays.toString( i ) ).append( "," );
        }
        osb.append( "]" );
        return "PreprocessedData{"
                + "ranks=" + Arrays.toString( ranks )
                + ", incomingShortcuts=" + isb.toString()
                + ", outgoingShortcuts=" + osb.toString()
                + ", sources=" + Arrays.toString( sources )
                + ", targets=" + Arrays.toString( targets )
                + ", startEdges=" + Arrays.toString( startEdges )
                + ", endEdges=" + Arrays.toString( endEdges ) + '}';
    }
}
