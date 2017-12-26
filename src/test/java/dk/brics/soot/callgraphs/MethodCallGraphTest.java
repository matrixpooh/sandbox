package dk.brics.soot.callgraphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.PackManager;
import soot.Transform;

public class MethodCallGraphTest {

	public static void main(String[] args) {
		 List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		 argsList.addAll(Arrays.asList(new String[] {
			 "-w", // whole program
			 "-pp", // prepend class path
			 //"-v", //way too verbose
			 "-src-prec", "class", //Try to resolve classes first from .class files found in the Soot classpath	
			 "-soot-class-path", "/Users/rizhiy/git/autolink-java/target/classes",
			 "-process-path", "/Users/rizhiy/git/autolink-java/target/classes", //load all classes in the dir and sub-dirs
		 }));
		
		 args = argsList.toArray(new String[0]);
		 
		 PackManager.v().getPack("cg").add(new Transform("cg.myTrans", 
				 new CallGraphWalker("org.nibor.autolink.internal.EmailScanner", 
						 		"org.nibor.autolink.LinkSpan scan(java.lang.CharSequence,int,int)")));
		
		soot.Main.main(args);
	}
}
