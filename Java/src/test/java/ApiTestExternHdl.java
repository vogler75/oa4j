/*
    OA4J - WinCC Open Architecture for Java
    Copyright (C) 2017 Andreas Vogler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
import at.rocworks.oa4j.jni.ExternHdlFunction;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

/**
 *
 * @author vogler
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
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, String.format("execute function=%s parameter=%s",  function, in.formatValue() ));
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
            JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "unhandled: "+function.toString());
        }


        return new DynVar(new IntegerVar(0));
    }
}
