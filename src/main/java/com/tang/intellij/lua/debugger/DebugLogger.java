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

import com.intellij.execution.ui.ConsoleViewContentType;
import org.jetbrains.annotations.NotNull;

/**
 * console logger
 * Created by tangzx on 2017/5/1.
 */
public interface DebugLogger {
    void print(@NotNull String text, @NotNull ConsoleViewContentType type);
    void println(@NotNull String text, @NotNull ConsoleViewContentType type);
    void error(@NotNull String text);
}
