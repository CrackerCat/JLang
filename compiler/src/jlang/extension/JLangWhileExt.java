//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.Node;
import polyglot.ast.While;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangWhileExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        While n = (While) node();

        LLVMBasicBlockRef cond = v.utils.buildBlock("while.cond");
        LLVMBasicBlockRef body = v.utils.buildBlock("while.body");
        LLVMBasicBlockRef end = v.utils.buildBlock("while.end");

        v.pushLoop(cond, end);

        // Conditional.
        LLVMBuildBr(v.builder, cond);
        LLVMPositionBuilderAtEnd(v.builder, cond);
        lang().translateLLVMConditional(n.cond(), v, body, end);

        // Body.
        LLVMPositionBuilderAtEnd(v.builder, body);
        n.visitChild(n.body(), v);
        v.utils.branchUnlessTerminated(cond);

        LLVMPositionBuilderAtEnd(v.builder, end);
        v.popLoop();
        return n;
    }
}
