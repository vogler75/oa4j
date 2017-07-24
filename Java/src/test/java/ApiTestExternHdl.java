import at.rocworks.oa4j.jni.ExternHdlFunction;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;

import java.util.logging.Level;

/**
 * Created by vogler on 2/20/2017.
 */
public class ApiTestExternHdl extends ExternHdlFunction {
    public ApiTestExternHdl(long waitCondPtr) {
        super(waitCondPtr);
    }

    /**
     *
     * @param function Name of a (sub)program unit, must be handled by yourself (e.g. case statement)
     * @param in List of parameter values
     * @return List of values
     */
    @Override
    public DynVar execute(TextVar function, DynVar in) {
        JDebug.out.log(Level.INFO, "execute function={0} parameter={1}", new Object[] { function, in.formatValue() });
        if (function.equals("TestDpTest")) {
            JDebug.out.info(function.toString());
            startFunc("jEvalScript",
                    new DynVar(new TextVar("main() { dpSet($1, $2); }"),
                            new DynVar(
                                    new TextVar("$1:ExampleDP_Arg1."),
                                    new TextVar("$2:0")),
                            new DynVar()));
        } else if (function.equals("NestedCall")) {
                JDebug.out.info(function.toString());
                TextVar script=new TextVar(
                        "main() { dyn_anytype out;"+
                        "int ret = javaCallAsync(\"ApiTestExternHdl\", \"Inside\", makeDynString(\"inside!\"), out);"+
                        "}");
                startFunc("jEvalScript", new DynVar(script, new DynVar(), new DynVar()));
        } else {
            JDebug.out.info("unhandled: "+function.toString());
        }


        return new DynVar(new IntegerVar(0));
    }
}
