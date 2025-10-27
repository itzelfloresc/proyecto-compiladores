package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LR1Automaton automaton;

    // merged LALR states and transitions
    private java.util.List<java.util.Set<LR1Item>> lalrStates = new java.util.ArrayList<>();
    private java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> lalrTransitions = new java.util.HashMap<>();
    
    // ACTION table: state -> terminal -> Action
    public static class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final com.compiler.parser.grammar.Production reduceProd; // for REDUCE

        private Action(Type type, Integer state, com.compiler.parser.grammar.Production prod) {
            this.type = type; this.state = state; this.reduceProd = prod;
        }

        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(com.compiler.parser.grammar.Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }
    }

    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> action = new java.util.HashMap<>();
    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> gotoTable = new java.util.HashMap<>();
    private final java.util.List<String> conflicts = new java.util.ArrayList<>();
    private int initialState = 0;

    public LALR1Table(LR1Automaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Builds the LALR(1) parsing table.
     */
    public void build() {
        // TODO: Implement the LALR(1) table construction logic.
        // This is a multi-step process.
        
        // Step 1: Ensure the underlying LR(1) automaton is built.
        // automaton.build();

        // Step 2: Merge LR(1) states to create LALR(1) states.
        //  a. Group LR(1) states that have the same "kernel" (the set of LR(0) items).
        //     - A kernel item is an LR(1) item without its lookahead.
        //     - Create a map from a kernel (Set<KernelEntry>) to a list of state IDs that share that kernel.
        //  b. For each group of states with the same kernel:
        //     - Create a single new LALR(1) state.
        //     - This new state is formed by merging the LR(1) items from all states in the group.
        //     - Merging means for each kernel item, the new lookahead set is the union of all lookaheads for that item across the group.
        //     - Store these new LALR states in `lalrStates`.
        //  c. Create a mapping from old LR(1) state IDs to new LALR(1) state IDs.

        // Step 3: Build the transitions for the new LALR(1) automaton.
        //  - For each transition in the original LR(1) automaton `s -X-> t`:
        //  - Add a new transition for the LALR automaton: `merged(s) -X-> merged(t)`.
        //  - Use the mapping from step 2c to find the merged state IDs.
        //  - Store these new transitions in `lalrTransitions`.

        // Step 4: Fill the ACTION and GOTO tables based on the LALR automaton.
        //  - Call a helper method, e.g., `fillActionGoto()`.
        automaton.build();
        Map<Set<KernelEntry>, List<Integer>> kernelGroups = new HashMap<>();
        List<Set<LR1Item>> lr1States = automaton.getStates();

        for (int i = 0; i < lr1States.size(); i++) {
            Set<KernelEntry> kernel = new HashSet<>();
            for (LR1Item item : lr1States.get(i)) {
                kernel.add(new KernelEntry(item.production, item.dotPosition));
            }
            kernelGroups.computeIfAbsent(kernel, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, Integer> lr1ToLalrStateMap = new HashMap<>();
        int lalrStateCount = 0;
        for (List<Integer> group : kernelGroups.values()) {
            Set<LR1Item> mergedState = new HashSet<>();
            for (int lr1StateIndex : group) {
                mergedState.addAll(lr1States.get(lr1StateIndex));
                lr1ToLalrStateMap.put(lr1StateIndex, lalrStateCount);
            }
            lalrStates.add(mergedState);
            lalrStateCount++;
        }

        this.initialState = lr1ToLalrStateMap.getOrDefault(0, 0);

        Map<Integer, Map<com.compiler.parser.grammar.Symbol, Integer>> lr1Transitions = automaton.getTransitions();
        for (Map.Entry<Integer, Map<com.compiler.parser.grammar.Symbol, Integer>> entry : lr1Transitions.entrySet()) {
            int fromLr1State = entry.getKey();
            int fromLalrState = lr1ToLalrStateMap.get(fromLr1State);

            for (Map.Entry<com.compiler.parser.grammar.Symbol, Integer> trans : entry.getValue().entrySet()) {
                com.compiler.parser.grammar.Symbol symbol = trans.getKey();
                int toLr1State = trans.getValue();
                int toLalrState = lr1ToLalrStateMap.get(toLr1State);

                lalrTransitions.computeIfAbsent(fromLalrState, k -> new HashMap<>()).put(symbol, toLalrState);
            }
        }

        fillActionGoto();
    }

    private void fillActionGoto() {
        // TODO: Populate the ACTION and GOTO tables based on the LALR states and transitions.
        // 1. Clear the action, gotoTable, and conflicts lists.
        // 2. Iterate through each LALR state `s` from 0 to lalrStates.size() - 1.
        // 3. For each state `s`, iterate through its LR1Item `it`.
        //    a. Get the symbol after the dot, `X = it.getSymbolAfterDot()`.
        //    b. If `X` is a terminal (SHIFT action):
        //       - Find the destination state `t` from `lalrTransitions.get(s).get(X)`.
        //       - Check for conflicts: if action table already has an entry for `[s, X]`, it's a conflict.
        //       - Otherwise, set `action[s][X] = SHIFT(t)`.
        //    c. If the dot is at the end of the production (`X` is null) (REDUCE or ACCEPT action):
        //       - This is an item like `[A -> α •, a]`.
        //       - If it's the augmented start production (`S' -> S •`) and lookahead is `$`, this is an ACCEPT action.
        //         - Set `action[s][$] = ACCEPT`.
        //       - Otherwise, it's a REDUCE action.
        //         - For the lookahead symbol `a` in the item:
        //         - Check for conflicts: if `action[s][a]` is already filled, report a Shift/Reduce or Reduce/Reduce conflict.
        //         - Otherwise, set `action[s][a] = REDUCE(A -> α)`.
        // 4. Populate the GOTO table.
        //    - For each state `s`, look at its transitions in `lalrTransitions`.
        //    - For each transition on a NON-TERMINAL symbol `B` to state `t`:
        //    - Set `gotoTable[s][B] = t`.
        action.clear();
        gotoTable.clear();
        conflicts.clear();

        for (int s = 0; s < lalrStates.size(); s++) {
            action.put(s, new HashMap<>());
            gotoTable.put(s, new HashMap<>());

            if (lalrTransitions.containsKey(s)) {
                for (Map.Entry<com.compiler.parser.grammar.Symbol, Integer> trans : lalrTransitions.get(s).entrySet()) {
                    com.compiler.parser.grammar.Symbol symbol = trans.getKey();
                    int t = trans.getValue();
                    if (symbol.type == com.compiler.parser.grammar.SymbolType.TERMINAL) {
                        action.get(s).put(symbol, Action.shift(t)); //SHIFT action
                    } else {
                        gotoTable.get(s).put(symbol, t);
                    }
                }
            }

            for (LR1Item item : lalrStates.get(s)) {
                if (item.getSymbolAfterDot() == null) { // Dot is at the end: [A -> α •, a]
                    com.compiler.parser.grammar.Symbol lookahead = item.lookahead;

                    // ACCEPT action.
                    if (item.production.left.name.equals(automaton.getAugmentedLeftName())) {
                        if (lookahead.name.equals("$")) {
                            action.get(s).put(lookahead, Action.accept());
                        }
                    } else { // REDUCE action.
                        Action reduceAction = Action.reduce(item.production);
                        Map<com.compiler.parser.grammar.Symbol, Action> stateActions = action.get(s);

                        // Check for conflicts
                        if (stateActions.containsKey(lookahead)) {
                            Action existingAction = stateActions.get(lookahead);
                            String conflictMsg;
                            if (existingAction.type == Action.Type.SHIFT) {
                                conflictMsg = String.format("Shift/Reduce conflict in state %d on symbol %s: SHIFT to %d vs REDUCE by %s",
                                        s, lookahead.name, existingAction.state, item.production);
                            } else { // REDUCE
                                conflictMsg = String.format("Reduce/Reduce conflict in state %d on symbol %s: REDUCE by %s vs REDUCE by %s",
                                        s, lookahead.name, existingAction.reduceProd, item.production);
                            }
                            conflicts.add(conflictMsg);
                        } else {
                            stateActions.put(lookahead, reduceAction);
                        }
                    }
                }
            }
        }
    }
    
    // ... (Getters and KernelEntry class can remain as is)
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> getActionTable() { return action; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getGotoTable() { return gotoTable; }
    public java.util.List<String> getConflicts() { return conflicts; }
    private static class KernelEntry {
        public final com.compiler.parser.grammar.Production production;
        public final int dotPosition;
        KernelEntry(com.compiler.parser.grammar.Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof KernelEntry)) return false;
            KernelEntry o = (KernelEntry) obj;
            return dotPosition == o.dotPosition && production.equals(o.production);
        }
        @Override
        public int hashCode() {
            int r = production.hashCode();
            r = 31 * r + dotPosition;
            return r;
        }
    }
    public java.util.List<java.util.Set<LR1Item>> getLALRStates() { return lalrStates; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getLALRTransitions() { return lalrTransitions; }
    public int getInitialState() { return initialState; }
}