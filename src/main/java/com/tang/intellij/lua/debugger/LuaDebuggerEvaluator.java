/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.debugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/5/1.
 */
public abstract class LuaDebuggerEvaluator extends XDebuggerEvaluator {
    @Nullable
    @Override
    public TextRange getExpressionRangeAtOffset(Project project, Document document, int offset, boolean sideEffectsAllowed) {
        final Ref<TextRange> currentRange = Ref.create(null);
        PsiDocumentManager.getInstance(project).commitAndRunReadAction(() -> {
            try {
                PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (file == null)
                    return;
                PsiElement element = file.findElementAt(offset);
                if (element == null || !element.isValid()) {
                    return;
                }
                IElementType type = element.getNode().getElementType();
                if (type == LuaTypes.ID) {
                    PsiElement parent = element.getParent();
                    PsiElement target = parent;
                    if (parent instanceof PsiNamedElement) {
                        if (parent instanceof PsiNameIdentifierOwner) {
                            if (parent instanceof LuaIndexExpr) {
                                target = parent;
                            } else {
                                target = ((PsiNameIdentifierOwner) parent).getNameIdentifier();
                            }
                        }
                    }

                    if (target != null)
                        currentRange.set(target.getTextRange());
                }
            } catch (IndexNotReadyException ignored) {}
        });
        return currentRange.get();
    }

    @Override
    public final void evaluate(@NotNull String express, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition) {
        //TODO self:method & self:method()
        //express = express.replace(':', '.');
        eval(express, xEvaluationCallback, xSourcePosition);
    }

    protected abstract void eval(@NotNull String express, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition);
}
