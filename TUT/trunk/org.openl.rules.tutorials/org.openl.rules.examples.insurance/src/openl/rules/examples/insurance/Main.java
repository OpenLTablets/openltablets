/*
 * Created on Dec 4, 2003
 *
 * Developed by OpenRules Inc. 2003
 */

package openl.rules.examples.insurance;

import org.openl.rules.runtime.RulesEngineFactory;
import org.openl.runtime.EngineFactory;


public class Main {
    
    public interface IExample {
        void main (String[] args);
    }
    
	public static void main(String[] args) {
		String fileName = "rules/Insurance.xls";
		EngineFactory<IExample> engineFactory = new RulesEngineFactory<IExample>(fileName, IExample.class);
        IExample instance = engineFactory.newEngineInstance();
		System.out.println(
			"\n============================================\n"
				+ fileName
				+ "(main)"
				+ "\n============================================\n");
		instance.main(new String[] { "" });
	}
}
