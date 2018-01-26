package an;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.EntryPoints;
import soot.Local;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

public class SootTest {
	
	public static final Logger log = LoggerFactory.getLogger(SootTest.class);
	
	@Test
	public void test() {
		
		loadClass("dk.brics.paddle.Item",false);
		loadClass("dk.brics.paddle.Container",false);
		SootClass c = loadClass("dk.brics.paddle.Test1",true);
		
		soot.Scene.v().loadNecessaryClasses();
		soot.Scene.v().setEntryPoints(EntryPoints.v().all());
		
		setSparkPointsToAnalysis();

		SootField f = getField("dk.brics.paddle.Container","item");		
		Map<Integer, Value> ls = getLocals(c, "go","dk.brics.paddle.Container");
		
		printLocalIntersects(ls);	
		printFieldIntersects(ls,f);		
	
		//Fetch the call graph
		CallGraph cg = soot.Scene.v().getCallGraph();
		if (cg.toString().contains(SootTest.class.getPackage().getName()))
			log.debug(cg.toString());
	
	}
	
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
	
	private static SootClass loadClass(String name, boolean main) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		if (main) Scene.v().setMainClass(c);
		return c;
	}
	
	static void setSparkPointsToAnalysis() {
		log.debug("[spark] Starting analysis ...");
				
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
		
		log.debug("[spark] Done!");
	}
	
	private static int getLineNumber(Stmt s) {
		Iterator<Tag> ti = s.getTags().iterator();
		while (ti.hasNext()) {
			Object o = ti.next();
			if (o instanceof LineNumberTag) 
				return Integer.parseInt(o.toString());
		}
		return -1;
	}
	
	private static SootField getField(String classname, String fieldname) {
		Collection<SootClass> app = Scene.v().getApplicationClasses();
		Iterator<SootClass> ci = app.iterator();
		while (ci.hasNext()) {
			SootClass sc = ci.next();
			if (sc.getName().equals(classname))
				return sc.getFieldByName(fieldname);
		}
		throw new RuntimeException("Field "+fieldname+" was not found in class "+classname);
	}
	
	private static Map<Integer, Value> getLocals(SootClass sc, String methodname, String typename) {
		Map<Integer, Value> res = new HashMap<Integer, Value>();
		Iterator<SootMethod> mi = sc.getMethods().iterator();
		while (mi.hasNext()) {
			SootMethod sm = (SootMethod)mi.next();
			log.debug("getLocals::soot method name:" + sm.getName());
			if (true && sm.getName().equals(methodname) && sm.isConcrete()) {
				JimpleBody jb = (JimpleBody)sm.retrieveActiveBody();
				Iterator<Unit> ui = jb.getUnits().iterator();
				while (ui.hasNext()) {
					Stmt s = (Stmt)ui.next();						
					int line = getLineNumber(s);
					// find definitions
					Iterator<ValueBox> bi = s.getDefBoxes().iterator();
					while (bi.hasNext()) {
						Object o = bi.next();
						if (o instanceof ValueBox) {
							Value v = ((ValueBox)o).getValue();
							if (v.getType().toString().equals(typename) && v instanceof Local)
								res.put(new Integer(line),v);
						}
					}					
				}
			}
		}
		
		return res;
	}
	
	private static void printLocalIntersects(Map<Integer, Value> ls) {
		soot.PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
		Iterator<Map.Entry<Integer,Value>> i1 = ls.entrySet().iterator();
		while (i1.hasNext()) {
			Map.Entry<Integer,Value> e1 = i1.next();
			int p1 = ((Integer)e1.getKey()).intValue();
			Local l1 = (Local)e1.getValue();
			PointsToSet r1 = pta.reachingObjects(l1);
			Iterator<Map.Entry<Integer,Value>> i2 = ls.entrySet().iterator();
			while (i2.hasNext()) {
				Map.Entry<Integer,Value> e2 = i2.next();
				int p2 = ((Integer)e2.getKey()).intValue();
				Local l2 = (Local)e2.getValue();
				PointsToSet r2 = pta.reachingObjects(l2);	
				if (p1<=p2)
					log.debug("["+p1+","+p2+"]\t Container intersect? "+r1.hasNonEmptyIntersection(r2));
			}
		}
	}
	
	private static void printFieldIntersects(Map<Integer, Value> ls, SootField f) {
		soot.PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
		Iterator<Map.Entry<Integer,Value>> i1 = ls.entrySet().iterator();
		while (i1.hasNext()) {
			Map.Entry<Integer,Value> e1 = i1.next();
			int p1 = ((Integer)e1.getKey()).intValue();
			Local l1 = (Local)e1.getValue();
			PointsToSet r1 = pta.reachingObjects(l1,f);
			Iterator<Map.Entry<Integer,Value>> i2 = ls.entrySet().iterator();
			while (i2.hasNext()) {
				Map.Entry<Integer,Value> e2 = i2.next();
				int p2 = ((Integer)e2.getKey()).intValue();
				Local l2 = (Local)e2.getValue();
				PointsToSet r2 = pta.reachingObjects(l2,f);	
				if (p1<=p2)
					log.debug("["+p1+","+p2+"]\t Container.item intersect? "+r1.hasNonEmptyIntersection(r2));
			}
		}
	}
	
}
