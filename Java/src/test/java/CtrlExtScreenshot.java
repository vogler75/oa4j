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
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class CtrlExtScreenshot extends ExternHdlFunction {
    public CtrlExtScreenshot(long waitCondPtr) {
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
       if (function.equals("Screenshot")) {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = null;
            try {
                capture = new Robot().createScreenCapture(screenRect);
                String ftype = in.size()>1 ? in.get(1).toString() : "jpg";
                String fname = in.size()>0 ? in.get(0).toString() : System.getProperty("java.io.tmpdir")+"/screenshot."+ftype;
                ImageIO.write(capture, ftype, new File(fname));
                return new DynVar(new IntegerVar(0), new TextVar(fname+"["+ftype+"]"));
            } catch (Exception e) {
                JDebug.StackTrace(Level.SEVERE, e);
            }
        } else {
            JDebug.out.info("unhandled: "+function.toString());
        }
        return new DynVar(new IntegerVar(0));
    }


}
