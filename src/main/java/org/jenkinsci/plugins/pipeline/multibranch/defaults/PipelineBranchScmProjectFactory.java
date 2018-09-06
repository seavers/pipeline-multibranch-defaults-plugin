/*
 * The MIT License
 *
 * Copyright (c) 2016 Saponenko Denis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.multibranch.defaults;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

/**
 * Recognizes and builds by default {@code Jenkinsfile}.
 */
public class PipelineBranchScmProjectFactory extends WorkflowBranchProjectFactory {
    public static final String SCRIPT = "Jenkinsfile";

    private SCM scm;
    private String scriptPath;
    private boolean lightweight;

    public Object readResolve() {
        if (this.scriptPath == null) {
            this.scriptPath = PipelineBranchScmProjectFactory.SCRIPT;
        }
        return this;
    }

    @DataBoundSetter
    public void setScm(SCM scm) {
        this.scm = scm;
    }

    @DataBoundSetter
    public void setScriptPath(String scriptPath) {
        if (StringUtils.isEmpty(scriptPath)) {
            this.scriptPath = SCRIPT;
        } else {
            this.scriptPath = scriptPath.trim();
        }
    }

    @DataBoundSetter
    public void setLightweight(boolean lightweight) {
        this.lightweight = lightweight;
    }

    public SCM getScm(){
        return scm;
    }

    public String getScriptPath(){
        return scriptPath;
    }

    public boolean isLightweight() {
        return lightweight;
    }

    @DataBoundConstructor
    public PipelineBranchScmProjectFactory() {
    }

    @Override
    protected FlowDefinition createDefinition() {
        CpsScmFlowDefinition definition = new CpsScmFlowDefinition(scm, scriptPath);
        definition.setLightweight(lightweight);
        return definition;
    }

    @Override
    protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            @Override
            public boolean isHead(Probe probe, TaskListener listener) throws IOException {
                return true;
            }
        };
    }

    @Extension
    public static class DescriptorDefaultImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {

        @Override
        public boolean isApplicable(Class<? extends MultiBranchProject> clazz) {
            return WorkflowMultiBranchProject.class.isAssignableFrom(clazz);
        }

        public Collection<? extends SCMDescriptor<?>> getApplicableDescriptors() {
            StaplerRequest req = Stapler.getCurrentRequest();
            Job<?,?> job = req != null ? req.findAncestorObject(Job.class) : null;
            return SCM._for(job);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "by Jiayun SCM";
        }

    }
}
