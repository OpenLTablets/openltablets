package org.openl.rules.ui;

import org.openl.types.IOpenMethod;
import org.openl.types.impl.IBenchmarkableMethod;

import org.openl.util.benchmark.BenchmarkUnit;
import org.openl.util.benchmark.Profiler;

import org.openl.vm.IRuntimeEnv;
import org.openl.vm.SimpleVM;


/**
 * DOCUMENT ME!
 *
 * @author Stanislav Shor
 */
public class WebStudioProfiler extends Profiler.Unit {
    String[] args;

    public static void main(String[] args) throws Exception {
        new WebStudioProfiler().run(args);
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg
     *
     * @throws Exception
     */
    private void run(String[] arg) throws Exception {
        this.args = arg;

        int N = 10;
        if (args.length > 3) {
            N = Integer.parseInt(args[3]);
        }

        new Profiler(N).profileUnit(this);
    }

    public BenchmarkUnit makeBenchMarkUnit() throws Exception {
        String project = args[0];
        String wrapperClass = args[1];
        String methodName = args[2];

        WebStudio studio = new WebStudio();
        String workspace = studio.getWorkspacePath();

        OpenLWebProjectInfo pi = new OpenLWebProjectInfo(workspace, project);

        OpenLWrapperInfo wrapper = new OpenLWrapperInfo(wrapperClass, pi);

        ProjectModel model = new ProjectModel(studio);

        model.setWrapperInfo(wrapper, false);

        Object instance = model.getWrapper().getInstance();
        IRuntimeEnv env = new SimpleVM().getRuntimeEnv();

        IOpenMethod m = model.getWrapper().getOpenClass().getMethod(methodName, null);

        if (m == null) {
            throw new Exception("Method " + methodName + " not found");
        }

        if (m instanceof IBenchmarkableMethod) {
            IBenchmarkableMethod bm = (IBenchmarkableMethod) m;
            return new BMMethodBenchmarkUnit(bm, instance, env);
        }
        return new MethodBenchmarkUnit(m, instance, env);
    }

    static class MethodBenchmarkUnit extends BenchmarkUnit {
        IOpenMethod m;
        Object instance;
        IRuntimeEnv env;
        Object[] params = {  };

        public MethodBenchmarkUnit(IOpenMethod m, Object instance, IRuntimeEnv env) {
            this.m = m;
            this.instance = instance;
            this.env = env;
        }

        protected void run() throws Exception {
            m.invoke(instance, params, env);
        }
    }

    static class BMMethodBenchmarkUnit extends MethodBenchmarkUnit {
/**
                 * @param m
                 * @param instance
                 * @param env
                 */
        public BMMethodBenchmarkUnit(IBenchmarkableMethod bm, Object instance,
            IRuntimeEnv env) {
            super(bm, instance, env);
        }

        public void runNtimes(int times) throws Exception {
            ((IBenchmarkableMethod) m).invokeBenchmark(instance, params, env, times);
        }
    }
}
