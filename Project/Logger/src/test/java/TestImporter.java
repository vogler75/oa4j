import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.types.api.EventItemAPI;
import at.rocworks.oa4j.logger.dbs.NoSQLCrateDB;
import at.rocworks.oa4j.logger.dbs.NoSQLElasticsearch;
import at.rocworks.oa4j.logger.dbs.NoSQLInfluxDB;
import at.rocworks.oa4j.logger.dbs.NoSQLMongoDB;
import at.rocworks.oa4j.logger.keys.KeyBuilder;
import at.rocworks.oa4j.logger.keys.KeyBuilderDp;
import at.rocworks.oa4j.logger.keys.KeyBuilderFixed;
import at.rocworks.oa4j.var.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class TestImporter {

    public static void main(String[] args) throws IOException {
        TestImporter imp = new TestImporter();
        System.out.println(imp.settings.getThreads());

        //String dir = "/Volumes/shared/export/backup";
        String dir = "S:/export/backup";
        Files.walk(Paths.get(dir))
                .filter(Files::isRegularFile)
                .filter((file) -> file.toString().endsWith(".out"))
                .forEach((file) -> imp.importFile(file.toString()));

        //imp.importFile("/Volumes/shared/export/backup/2013/12/2013.12.31.07-1388556000.out");
    }

    Properties props = new Properties();
    NoSQLSettings settings = new NoSQLSettings(props, "Importer");

    KeyBuilder keybuilder = new KeyBuilderFixed("EVENT");
    //KeyBuilder keybuilder = new KeyBuilderDp();

    ExecutorService executor = Executors.newFixedThreadPool(4);
    final int BLOCKSIZE=10000;

    NoSQLServer db = new NoSQLInfluxDB(settings, keybuilder, "http://ubuntu1:8086", "test");
    //NoSQLServer db = new NoSQLCrateDB(settings, "jdbc:crate://ubuntu1:5432/", "", "", "EVENT");
    //NoSQLServer db = new NoSQLElasticsearch(settings, "/Users/vogler", "elasticsearch", "ubuntu1", "scada", false);
    //NoSQLServer db = new NoSQLMongoDB(settings, "mongodb://ubuntu1:27017/", "scada", false);

    public void importFile(String file) {
        int count=0;
        Date t1 = new Date();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file.toString()));
            String line;
            try {
                List<Future> futures = new ArrayList<>();
                DataList events = new DataList(BLOCKSIZE);
                while((line = reader.readLine()) != null) {
                    String[] cols = line.split(",");
                    String dp="System:"+cols[0];
                    Long time = Long.valueOf(cols[1])/1000000; // nano to milli
                    Variable val;
                    switch (cols[7]) {
                        case "float": val = new FloatVar(Double.valueOf(cols[8])); break;
                        case "integer": val = new IntegerVar(Integer.valueOf(cols[8])); break;
                        case "time": val = new TimeVar(Long.valueOf(cols[8])); break;
                        case "bool": val = new BitVar(Integer.valueOf(cols[8])); break;
                        case "bit32": val = new Bit32Var(Long.valueOf(cols[8])); break;
                        case "bit64": val = new Bit32Var(Long.valueOf(cols[8])); break;
                        case "text": val = new TextVar(cols.length>8 ? cols[8] : ""); break;
                        default: val=null;
                    }
                    if (val!=null) {
                        count++;
                        events.addItem(new EventItemAPI(dp, val, time, 0, 0, 0));
                        if (events.isFull()) {
                            final DataList list = events;
                            Callable task = () -> db.storeData(list);
                            futures.add(executor.submit(task));
                            events = new DataList(BLOCKSIZE);
                        }
                    }
                }
                futures.forEach((future) -> {
                    try {
                        future.get();
                        System.out.print(".");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("");
        Date t2 = new Date();
        Double sec = ((t2.getTime()-t1.getTime())/1000.0);
        System.out.println(file+"..."+(sec > 0 ? (count / sec) : 0));
    }
}