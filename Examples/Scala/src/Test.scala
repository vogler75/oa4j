import at.rocworks.oa4j.base._
import at.rocworks.oa4j.`var`._

/**
  * Created by vogler on 2/21/2017.
  */
object Test {
  def main(args: Array[String]) {
    // add path to WCCOAjava.dll to your path environment!
    // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
    val m = (new JManager).init(args)
    m.start()
    JDebug.out.info("runSet...")
    runSet()
    JDebug.out.info("runGet...")
    runGet()
    JDebug.out.info("runConnect...")
    runConnect()
    JDebug.out.info("stop...")
    m.stop()
    JDebug.out.info("done.")
  }

  def runGet(): Unit = {
    val v1 = JClient.dpGet.add("ExampleDP_Trend1.").await()
    JDebug.out.info("v1: "+v1.toString)

    val v2 = JClient.dpGet("ExampleDP_Trend1.");
    JDebug.out.info("v2: "+v2.toString)

    val v3var = new VariablePtr()
    val v3ret = JClient.dpGet("ExampleDP_Trend1.", v3var);
    JDebug.out.info(s"v3: ret=$v3ret value="+v3var.get.toString)
  }

  def runSet(): Unit = {
    JClient.dpSet()
      .add("ExampleDP_Trend1.", new FloatVar(1))
      .add("ExampleDP_SumAlert.", "hello world!")
      .send()
    Thread.sleep(1000);

    JClient.dpSet("ExampleDP_Trend1.", 2.0);
    Thread.sleep(1000);

    JClient.dpSet("ExampleDP_Trend1.", new FloatVar(3))
    Thread.sleep(1000);

    JClient.dpSetWait("ExampleDP_SumAlert.", "hello scala world!")
    Thread.sleep(1000);
  }

  def runConnect() {
    JDebug.out.info("dpConnect...")

    val conn = JClient.dpConnect.add("ExampleDP_Trend1.")

    conn.answer((answer) => {
        JDebug.out.info("--- ANSWER BEG ---")
        JDebug.out.info(answer.toString())
        JDebug.out.info("--- ANSWER END ---")
      }
    )

    conn.hotlink((hotlink) => {
        JDebug.out.info("--- HOTLINK BEG ---")
        JDebug.out.info(hotlink.toString())
        JDebug.out.info("--- HOTLINK END ---")
      }
    )

    conn.connect()
    JDebug.out.info("sleep...")
    Thread.sleep(1000 * 60)
    JDebug.out.info("done")
    conn.disconnect()
  }
}
