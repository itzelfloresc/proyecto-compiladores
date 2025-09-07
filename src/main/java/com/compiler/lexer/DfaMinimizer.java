
/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Queue;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;


/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
        public DfaMinimizer() {
            // TODO: Implement constructor if needed
        }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        // TODO: Implement minimizeDfa
        /*
        Pseudocode:
        1. Collect and sort all DFA states
        2. Initialize table of state pairs; mark pairs as distinguishable if one is final and the other is not
        3. Iteratively mark pairs as distinguishable if their transitions lead to distinguishable states or only one has a transition
        4. Partition states into equivalence classes (using union-find)
        5. Create new minimized states for each partition
        6. Reconstruct transitions for minimized states
        7. Set start state and return minimized DFA
        */
        
        // Encontrar todos los estados alcanzables desde el estado inicial
        List<DfaState> allStates = new ArrayList<>();
        Set<DfaState> visited = new HashSet<>();
        Queue<DfaState> queue = new java.util.LinkedList<>();

        queue.add(originalDfa.startState);
        visited.add(originalDfa.startState);

        while (!queue.isEmpty()) {
            DfaState currentState = queue.poll();
            allStates.add(currentState);
            currentState.getTransitions().values().stream().filter(visited::add).forEach(queue::add);
        }
            
        // 1. Inicializar tabla de pares de estados
        Map<Pair, Boolean> table = new HashMap<>();
            
        // 2. Marcar pares inicialmente distinguibles (uno final, otro no)
        for (DfaState s1 : allStates) {
            for (DfaState s2 : allStates) {
                if (s1.id < s2.id) {
                    Pair pair = new Pair(s1, s2);
                    boolean distinguishable = (s1.isFinal() != s2.isFinal());
                    table.put(pair, distinguishable);
                }
            }
        }
            
        // 3. Iterativamente marcar pares distinguibles
        boolean changed = true;
        while (changed) {
            changed = false;
                
            for (DfaState s1 : allStates) {
                for (DfaState s2 : allStates) {
                    if (s1.id < s2.id) {
                        Pair pair = new Pair(s1, s2);
                        if (!table.get(pair)) { // Si no están marcados como distinguibles                            
                            // Verificar si sus transiciones llevan a estados distinguibles
                            for (char symbol : alphabet) {
                                DfaState next1 = s1.getTransition(symbol);
                                DfaState next2 = s2.getTransition(symbol);

                                if ((next1 == null) != (next2 == null)) {
                                    table.put(pair, true);
                                    changed = true;
                                    break;
                                } else if (next1 != null && next2 != null && !next1.equals(next2)) {
                                    Pair nextPair = new Pair(next1, next2);
                                    if (table.getOrDefault(nextPair, false)) {
                                        table.put(pair, true);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            
        // 4. Crear particiones de estados equivalentes
        List<Set<DfaState>> partitions = createPartitions(allStates, table);
            
            // 5. Crear nuevos estados minimizados
        Map<DfaState, DfaState> stateToMinimized = new HashMap<>();
        List<DfaState> minimizedStates = new ArrayList<>();
            
        for (Set<DfaState> partition : partitions) {
            // Crear nuevo estado minimizado
            DfaState minimizedState = new DfaState();
                
            // Marcar como final si algún estado original era final
            boolean isFinal = partition.stream().anyMatch(DfaState::isFinal);
            minimizedState.setFinal(isFinal);
                
            minimizedStates.add(minimizedState);
                
            // Mapear estados originales al minimizado
            for (DfaState originalState : partition) {
                stateToMinimized.put(originalState, minimizedState);
            }
        }
            
        // 6. Reconstruir transiciones
        for (DfaState originalState : allStates) {
            DfaState minimizedState = stateToMinimized.get(originalState);
                
            for (Map.Entry<Character, DfaState> transition : originalState.getTransitions().entrySet()) {
                char symbol = transition.getKey();
                DfaState originalTarget = transition.getValue();
                DfaState minimizedTarget = stateToMinimized.get(originalTarget);
                    
                minimizedState.addTransition(symbol, minimizedTarget);
            }
        }
            
        // 7. Encontrar estado inicial minimizado
        DfaState minimizedStartState = stateToMinimized.get(originalDfa.startState);
            
        return new DFA(minimizedStartState, minimizedStates);
        
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        // TODO: Implement createPartitions
        /*
        Pseudocode:
        1. Initialize each state as its own parent
        2. For each pair not marked as distinguishable, union the states
        3. Group states by their root parent
        4. Return list of partitions
        */
        Map<DfaState, DfaState> parent = new HashMap<>();
        for (DfaState state : allStates) {
            parent.put(state, state);
        }

        for (DfaState s1 : allStates){
            for (DfaState s2 : allStates) {
                if (s1.id < s2.id) {
                    Pair pair = new Pair(s1, s2);
                    if (!table.getOrDefault(pair, false)) {
                        union(parent, s1, s2);
                    }
                }
            }
        }

        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState state : allStates) {
            DfaState root = find(parent, state);
            groups. computeIfAbsent(root, k -> new HashSet<>()).add(state);
        }

        return new ArrayList<>(groups.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        // TODO: Implement find
        /*
        Pseudocode:
        If parent[state] == state, return state
        Else, recursively find parent and apply path compression
        Return parent[state]
        */
        if(parent.get(state) == state) {
            return state;
        }

        DfaState root = find(parent, parent.get(state));
        parent.put(state, root);
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        // TODO: Implement union
        /*
        Pseudocode:
        Find roots of s1 and s2
        If roots are different, set parent of one to the other
        */
       DfaState root1 = find(parent, s1);
       DfaState root2 = find(parent, s2);

       if(root1 != root2) {
        parent.put(root1, root2);
       }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            // TODO: Implement Pair constructor
            /*
             Pseudocode:
             Assign s1 and s2 so that s1.id <= s2.id
            */
            if(s1.id <= s2.id){
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            // TODO: Implement equals
            /*
             Pseudocode:
             Return true if both s1 and s2 ids match
            */
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return s1.id == pair.s1.id && s2.id == pair.s2.id;
        }

        @Override
        public int hashCode() {
            // TODO: Implement hashCode
            /*
             Pseudocode:
             Return hash of s1.id and s2.id
            */
            return Objects.hash(s1.id, s2.id);
        }
    }
}