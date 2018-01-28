package an;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;

public class CallGraphTest {

	@Test
	public void test() {
		Options.v().set_keep_line_number(true);

		Options.v().set_whole_program(true);

		/* cg - call graph option */
		Options.v().setPhaseOption("cg", "verbose:true");

		/* where to look for classes */
		Options.v().set_soot_classpath("./target/test-classes/");
		Options.v().set_prepend_classpath(true);

		/* these 2 options prevent from analyzing jdk dependencies */
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		
		/* load all classes in dir */
		Options.v().set_process_dir(Arrays.asList(new String[]{"./target/test-classes/"}));

		Scene.v().loadNecessaryClasses();

		CHATransformer.v().transform();
		CallGraph cg = Scene.v().getCallGraph();
		Assert.assertNotNull(cg);
		
		SootClass a = Scene.v().loadClassAndSupport("dk.brics.paddle.Container");
		SootMethod m = a.getMethod("void setItem(dk.brics.paddle.Item)");
		
		printPossibleCallers(cg, m);
	}

	public void printPossibleCallers(CallGraph cg, SootMethod target) {
		Iterator sources = new Sources(cg.edgesInto(target));
		while (sources.hasNext()) {
			SootMethod src = (SootMethod) sources.next();
			System.out.println(target + " might be called by " + src);
		}
	}

	private static SootClass loadClass(String name, boolean main) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		if (main)
			Scene.v().setMainClass(c);
		return c;
	}
}
