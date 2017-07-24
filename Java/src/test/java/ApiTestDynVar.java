import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.var.Variable;

/**
 * Created by vogler on 2/21/2017.
 */
public class ApiTestDynVar {
    public static void main(String[] args) throws Exception {
        DynVar dv = new DynVar(new IntegerVar(1), new TextVar("hello"));
        dv.forEach((Variable v)->System.out.println(v));
    }
}
