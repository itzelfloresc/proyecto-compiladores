package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.compiler.lexer.token.Token;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;
import com.compiler.parser.lr.LALR1Table.Action;
/**
 * Implements the LALR(1) parsing engine.
 * Uses a stack and the LALR(1) table to process a sequence of tokens.
 * Complementary task for Practice 9.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

   // package-private accessor for tests
   LALR1Table getTable() {
       return table;
   }

   /**
    * Parses a sequence of tokens using the LALR(1) parsing algorithm.
    * @param tokens The list of tokens from the lexer.
    * @return true if the sequence is accepted, false if a syntax error is found.
    */
   public boolean parse(List<Token> tokens) {
        // TODO: Implement the LALR(1) parsing algorithm.
        // 1. Initialize a stack for states and push the initial state (from table.getInitialState()).
        // 2. Create a mutable list of input tokens from the parameter and add the end-of-input token ("$").
        // 3. Initialize an instruction pointer `ip` to 0, pointing to the first token.
        // 4. Start a loop that runs until an ACCEPT or ERROR condition is met.
        //    a. Get the current state from the top of the stack.
        //    b. Get the current token `a` from the input list at index `ip`.
        //    c. Look up the action in the ACTION table: action = table.getActionTable()[state][a.type].
        //    d. If no action is found (it's null), it's a syntax error. Return false.
        //    e. If the action is SHIFT(s'):
        //       i. Push the new state s' onto the stack.
        //       ii. Advance the input pointer: ip++.
        //    f. If the action is REDUCE(A -> β):
        //       i. Pop |β| symbols (and states) from the stack. Handle epsilon productions (where |β|=0).
        //       ii. Get the new state `s` from the top of the stack.
        //       iii. Look up the GOTO state: goto_state = table.getGotoTable()[s][A].
        //       iv. If no GOTO state is found, it's an error. Return false.
        //       v. Push the goto_state onto the stack.
        //    g. If the action is ACCEPT:
        //       i. The input has been parsed successfully. Return true.
        //    h. If the action is none of the above, it's an unhandled case or error. Return false.
        
        Stack<Integer> stack = new Stack<>();
        stack.push(table.getInitialState());

        List<Token> inputTokens = new ArrayList<>(tokens);
        inputTokens.add(new Token(-1, "$"));

        int ip = 0;
        while(true) {
            int state = stack.peek();
            Token currentToken = inputTokens.get(ip);
            Symbol currentSymbol = new Symbol(currentToken.getName(), SymbolType.TERMINAL);

            Map<Symbol, Action> stateActions = table.getActionTable().get(state);
            if (stateActions == null)
                return false;
            
            Action action = stateActions.get(currentSymbol);
            if(action == null)
                return false;

            if (action.type == Action.Type.SHIFT) {
                stack.push(action.state);
                ip++;
            } else if (action.type == Action.Type.REDUCE) {
                Production production = action.reduceProd;
                int betaLength = production.right.size();
                for (int i = 0; i < betaLength; i++)
                    stack.pop();
                int newState = stack.peek();
                Integer goToState = table.getGotoTable().get(newState).get(production.left);
                if (goToState == null)
                    return false;
                stack.push(goToState);
            } else if (action.type == Action.Type.ACCEPT) {
                return true;
            } else {
                return false;
            }
        }
   }
}