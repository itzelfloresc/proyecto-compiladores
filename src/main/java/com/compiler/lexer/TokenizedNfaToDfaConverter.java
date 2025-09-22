package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * TokenizedNfaToDfaConverter
 * -------------------------
 * Utility class for converting NFAs with token information to DFAs using the subset construction algorithm.
 * Preserves token information during the conversion process.
 */
public class TokenizedNfaToDfaConverter {

    /**
     * Default constructor for TokenizedNfaToDfaConverter.
     */
    public TokenizedNfaToDfaConverter() {
        // Default constructor
    }

    /**
     * Converts an NFA (potentially with tokens) to a DFA using the subset construction algorithm.
     * Token information is preserved - if multiple tokens are present in a DFA state,
     * the token with the lowest ID takes precedence.
     *
     * @param nfa The input NFA
     * @param alphabet The input alphabet (set of characters)
     * @return The resulting DFA with token information preserved
     */
    public static DFA convertNfaToDfaWithTokens(NFA nfa, Set<Character> alphabet) {
        // 1. Crear estado inicial DFA desde epsilon-closure del estado inicial NFA
        Set<State> initialNfaStates = new HashSet<>();
        initialNfaStates.add(nfa.getStartState());
        Set<State> initialClosure = epsilonClosure(initialNfaStates);
        
        DfaState startDfaState = new DfaState(initialClosure);
        
        // Lista para almacenar todos los estados DFA
        List<DfaState> allDfaStates = new ArrayList<>();
        allDfaStates.add(startDfaState);
        
        // Cola para procesar estados DFA no marcados
        Queue<DfaState> unmarkedStates = new LinkedList<>();
        unmarkedStates.offer(startDfaState);

        // 2. Procesar mientras haya estados no marcados
        while (!unmarkedStates.isEmpty()) {
            DfaState currentDfaState = unmarkedStates.poll();
            
            // Para cada símbolo en el alfabeto
            for (char symbol : alphabet) {
                // Calcular move y epsilon-closure
                Set<State> moveResult = move(currentDfaState.getName(), symbol);
                Set<State> targetNfaStates = epsilonClosure(moveResult);
                
                // Si el conjunto resultante no está vacío
                if (!targetNfaStates.isEmpty()) {
                    // Buscar si ya existe un estado DFA para este conjunto
                    DfaState targetDfaState = findDfaState(allDfaStates, targetNfaStates);
                    
                    // Si no existe, crear uno nuevo
                    if (targetDfaState == null) {
                        targetDfaState = new DfaState(targetNfaStates);
                        allDfaStates.add(targetDfaState);
                        unmarkedStates.offer(targetDfaState);  // Agregar para procesar
                    }
                    
                    // Agregar transición del estado actual al destino
                    currentDfaState.addTransition(symbol, targetDfaState);
                }
            }
        }
        
        // 3. Marcar estados DFA como finales si contienen algún estado NFA final
        // Token information is already handled in DfaState constructor
        for (DfaState dfaState : allDfaStates) {
            boolean isFinal = false;
            for (State nfaState : dfaState.getName()) {
                if (nfaState.isFinal()) {
                    isFinal = true;
                    break;
                }
            }
            if (isFinal && !dfaState.isFinal()) {
                dfaState.setFinal(true);
            }
        }
        
        // 4. Retornar DFA completo
        return new DFA(startDfaState, allDfaStates);
    }

    /**
     * Computes the epsilon-closure of a set of NFA states.
     * The epsilon-closure is the set of states reachable by epsilon (null) transitions.
     *
     * @param states The set of NFA states.
     * @return The epsilon-closure of the input states.
     */
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);  // Inicializar con estados de entrada
        Stack<State> stack = new Stack<>();
        
        // Agregar todos los estados iniciales a la pila
        for (State state : states) {
            stack.push(state);
        }
        
        // Procesar mientras haya estados en la pila
        while (!stack.isEmpty()) {
            State current = stack.pop();
            
            // Obtener todas las transiciones epsilon del estado actual
            List<State> epsilonTransitions = current.getEpsilonTransitions();
            
            // Para cada estado alcanzable por epsilon
            for (State nextState : epsilonTransitions) {
                // Si no está ya en el cierre, agregarlo
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    stack.push(nextState);  // Procesar también este estado
                }
            }
        }
        
        return closure;
    }

    /**
     * Returns the set of states reachable from a set of NFA states by a given symbol.
     *
     * @param states The set of NFA states.
     * @param symbol The input symbol.
     * @return The set of reachable states.
     */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();
    
        // Para cada estado en el conjunto de entrada
        for (State state : states) {
            // Obtener todos los estados alcanzables con el símbolo dado
            List<State> transitions = state.getTransitions(symbol);
            
            // Agregar todos los estados destino al resultado
            for (State nextState : transitions) {
                result.add(nextState);
            }
        }
        
        return result;
    }

    /**
     * Finds an existing DFA state representing a given set of NFA states.
     *
     * @param dfaStates The list of DFA states.
     * @param targetNfaStates The set of NFA states to search for.
     * @return The matching DFA state, or null if not found.
     */
    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState dfaState : dfaStates) {
            // Si el conjunto de estados NFA es igual al objetivo
            if (dfaState.getName().equals(targetNfaStates)) {
                return dfaState;
            }
        }
        return null;
    }
}