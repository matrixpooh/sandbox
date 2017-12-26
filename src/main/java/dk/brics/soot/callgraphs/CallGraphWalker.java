package dk.brics.soot.callgraphs;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

final class CallGraphWalker extends SceneTransformer {

	final String type;
	final String method;
	public CallGraphWalker(String entryClass, String entryMethod){
		this.type = entryClass;
		this.method = entryMethod;
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		CHATransformer.v().transform();

		SootClass a = Scene.v().loadClassAndSupport(this.type);
		// SootMethod m = a.getMethodByName("scan");
		SootMethod m = a.getMethod(this.method);

		CallGraph cg = Scene.v().getCallGraph();

		Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(m));
		while (targets.hasNext()) {
			SootMethod tgt = (SootMethod) targets.next();
			System.out.println(m + " may call " + tgt);
		}

		Iterator<Edge> sources = cg.edgesInto(m);
		while (sources.hasNext()) {
			Edge src = sources.next();
			System.out.println(m + " is called by " + src);
		}

		// Retrieve the method and its body
		Body b = m.retrieveActiveBody();
		// Build the CFG and run the analysis
		UnitGraph g = new ExceptionalUnitGraph(b);
		// Iterate over the results
		Iterator<Unit> i = g.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			System.out.println("unit:" + u);
		}
	}
}