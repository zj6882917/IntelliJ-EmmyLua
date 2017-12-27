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

package com.tang.intellij.lua.debugger.app;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.tang.intellij.lua.debugger.remote.LuaMobDebugProcess;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;

class LuaAppMobProcess extends LuaMobDebugProcess {
    private final LuaAppRunConfiguration configuration;
    private boolean isStopped;

    LuaAppMobProcess(@NotNull XDebugSession session) {
        super(session);
        configuration = (LuaAppRunConfiguration) session.getRunProfile();
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        StringBuilder setupPackagePath = new StringBuilder(String.format("%s/?.lua;", LuaFileUtil.getPluginVirtualFile("debugger/mobdebug")));

        Module[] modules = ModuleManager.getInstance(getSession().getProject()).getModules();
        for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                String path = sourceRoot.getCanonicalPath();
                if (path != null) {
                    setupPackagePath.append(path).append("/?.lua;");
                }
            }
        }

        GeneralCommandLine commandLine = new GeneralCommandLine(configuration.getProgram());
        commandLine.addParameters("-e", String.format("package.path = package.path .. ';%s' require('mobdebug').start()", setupPackagePath.toString()));
        commandLine.addParameters(configuration.getFile());

        String dir = configuration.getWorkingDir();
        if (dir != null && !dir.isEmpty())
            commandLine.setWorkDirectory(dir);

        try {
            OSProcessHandler handler = new OSProcessHandler(commandLine);
            handler.addProcessListener(new ProcessListener() {
                @Override
                public void startNotified(ProcessEvent processEvent) {

                }

                @Override
                public void processTerminated(ProcessEvent processEvent) {
                    if (!isStopped)
                        getSession().stop();
                }

                @Override
                public void processWillTerminate(ProcessEvent processEvent, boolean b) {

                }

                @Override
                public void onTextAvailable(ProcessEvent processEvent, Key key) {
                    if (key == ProcessOutputTypes.STDOUT) {
                        print(processEvent.getText(), ConsoleViewContentType.NORMAL_OUTPUT);
                    } else if (key == ProcessOutputTypes.STDERR) {
                        error(processEvent.getText());
                    }
                }
            });
            handler.startNotify();
        } catch (Exception e) {
            error(e.getMessage());
            getSession().stop();
        }
    }

    @Override
    public void stop() {
        isStopped = true;
        super.stop();
    }
}
