package an;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.EntryPoints;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

public class GraphTest {

	public static final Logger log = LoggerFactory.getLogger(GraphTest.class);
	
	// Make sure we get line numbers and whole program analysis
		static {
			soot.options.Options.v().set_keep_line_number(true);
			soot.options.Options.v().set_whole_program(true);
			soot.options.Options.v().setPhaseOption("cg","verbose:true");
			soot.options.Options.v().set_soot_classpath("./target/test-classes/");
			soot.options.Options.v().set_prepend_classpath(true);
			soot.options.Options.v().set_no_bodies_for_excluded(true);
			soot.options.Options.v().set_allow_phantom_refs(true);
		}
		
	public static void main(String[] args) {
		
		CHATransformer.v().transform();

		SootClass a = Scene.v().loadClassAndSupport("dk.brics.paddle.Container");
		SootMethod m = a.getMethod("dk.brics.paddle.Item getItem()");
		
		Scene.v().loadNecessaryClasses();
		Scene.v().setEntryPoints(EntryPoints.v().all());
		
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("enabled","true");
		opt.put("verbose","true");
		opt.put("ignore-types","false");          
		opt.put("force-gc","false");            
		opt.put("pre-jimplify","false");          
		opt.put("vta","false");                   
		opt.put("rta","false");                   
		opt.put("field-based","false");           
		opt.put("types-for-sites","false");        
		opt.put("merge-stringbuffer","true");   
		opt.put("string-constants","false");     
		opt.put("simulate-natives","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            
		opt.put("simplify-offline","false");    
		opt.put("simplify-sccs","false");        
		opt.put("ignore-types-for-sccs","false");
		opt.put("propagator","worklist");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid");
		opt.put("dump-html","false");           
		opt.put("dump-pag","false");             
		opt.put("dump-solution","false");        
		opt.put("topo-sort","false");           
		opt.put("dump-types","true");             
		opt.put("class-method-var","true");     
		opt.put("dump-answer","false");          
		opt.put("add-tags","false");             
		opt.put("set-mass","false"); 		
		
		SparkTransformer.v().transform("",opt);
		CallGraph cg = Scene.v().getCallGraph();

		Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesInto(m));
		while (targets.hasNext()) {
			SootMethod tgt = (SootMethod) targets.next();
			log.info("printPossibleCallees::"+ m + " may call " + tgt);
		}		
	}
}
