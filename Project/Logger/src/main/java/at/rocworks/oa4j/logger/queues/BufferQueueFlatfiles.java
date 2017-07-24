/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.queues;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.logging.Level;

import at.rocworks.oa4j.base.JSemaphore;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JSemaphore;

/**
 *
 * @author vogler
 */
//-----------------------------------------------------------------------------------------
public class BufferQueueFlatfiles implements Runnable, IBufferQueue {
    // TODO check disk space

    private final String queueName;
    
    private volatile int id_in = 0;
    private volatile int id_out = 0;

    private volatile int lastPeekId = 0;
    private volatile DataList lastPeekBlock = null;
    
    private int nextWriteId = 0;
    private DataList nextWriteBlock = null;
    
    private int currentWritingId = 0;
    private DataList currentWritingBlock = null;
    
    private final JSemaphore lastWrittenId;
    private DataList lastWrittenBlock = null;
    
    private final JSemaphore PushObjectAvailable = new JSemaphore(false);
    private final JSemaphore PushObjectFree = new JSemaphore(true);

    private final String directory;

    public BufferQueueFlatfiles(String queueName, String path) {
        this.queueName = queueName;
        this.directory = path + "/" + queueName;
        (new File(path)).mkdir();
        (new File(this.directory)).mkdir();        
        this.lastWrittenId = new JSemaphore();
    }
    
    @Override
    public void start() {
        getFilesState();
        new Thread(this).start();   
    }

    @Override
    public long size() {
        return id_out == 0 ? 0 : (id_in - id_out + 1);
    }
 

    @Override
    public DataList peek() {
        int id;
        synchronized (this) {
            while ( true ) {
                id = id_out;

                if (id == 0) {
                    return null;
                }

                if (id == lastPeekId) {
                    JDebug.out.log(Level.FINE, "{0} cached block #{1} read {2}", new Object[]{queueName, id, lastPeekBlock});
                    return lastPeekBlock;
                }

                if (id == nextWriteId) {
                    JDebug.out.log(Level.FINE, "{0} getting next write block #{1}...", new Object[]{queueName,id});
                    lastPeekId = id;
                    lastPeekBlock = nextWriteBlock;
                    return lastPeekBlock;
                }                   

                if (id == currentWritingId) {
                    JDebug.out.log(Level.FINE, "{0} getting current writing block #{1}...", new Object[]{queueName,id});
                    lastPeekId = id;
                    lastPeekBlock = currentWritingBlock;
                    return lastPeekBlock;
                }        

                if (id == lastWrittenId.getValue()) {
                    JDebug.out.log(Level.FINE, "{0} getting last written block #{1}...", new Object[]{queueName,id});
                    lastPeekId = id;
                    lastPeekBlock = lastWrittenBlock;
                    return lastPeekBlock;
                }            

                if ( readFile(id) ) {
                    return lastPeekBlock;                    
                } else { // error reading file, delete it...
                    deleteFile(id);
                    setNextOutputBlock();                    
                }
            }
        }
    }

    @Override
    public DataList pop() {
        DataList block = peek();
        if (block != null) {
            lastWrittenId.awaitGT(id_out);
            deleteFile(id_out);
            synchronized (this) {
                lastPeekId = 0;
                lastPeekBlock = null;
                setNextOutputBlock();
            }
        }
        return block;
    }
    
    @Override
    public boolean push(DataList block) {     
        PushObjectFree.request();
        synchronized (this) {
            nextWriteId = ++id_in;
            nextWriteBlock = block;
            if (id_out == 0) {
                id_out = nextWriteId;
            }
        }
        PushObjectAvailable.dispatch();
        return true;
    }    

    @Override
    public void run() {
        while (true) {
            PushObjectAvailable.request();
            synchronized (this) {
                currentWritingId = nextWriteId;
                currentWritingBlock = nextWriteBlock;
                nextWriteId = 0;
                nextWriteBlock = null;
            }
            PushObjectFree.dispatch();
            writeFile(currentWritingId, currentWritingBlock);
            synchronized (this) {
                lastWrittenId.setValue(currentWritingId);
                lastWrittenBlock = currentWritingBlock;
                currentWritingId = 0;
                currentWritingBlock = null;
            }
        }
    }
    
    private void setNextOutputBlock() {
        id_out = (id_out == id_in ? 0 : id_out + 1);        
    }
    
    private String getFileName(int id) {
        return directory + "/" + String.format("%010d.BUF", id);
    }   
    
    private void getFilesState() {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles((File dir, String name) -> name.toUpperCase().endsWith(".BUF"));
        int k=0;
        for (File f : listOfFiles) {
            //JDebug.out.info(f.toString());
            try {
                int nr = Integer.parseInt(f.getName().substring(0, f.getName().length()-4));
                if ( k++==0 ) id_in=id_out=nr;
                else {
                    id_in=nr>id_in?nr:id_in;
                    id_out=nr<id_out?nr:id_out;
                }
            }catch ( NumberFormatException ex ) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        JDebug.out.log(Level.INFO, "{0} getFilesState: id_in: {1} id_out: {2}", new Object[]{queueName, id_in, id_out});
    }
    
    private boolean writeFile(int id, DataList block) {
        try (FileOutputStream fileOutDat = new FileOutputStream(getFileName(id), false);
                ObjectOutputStream objOutDat = new ObjectOutputStream(fileOutDat)) {
            objOutDat.writeObject(block);
            JDebug.out.log(Level.FINE, "{0} file #{1} saved (id_in={2} id_out={3})", new Object[]{queueName, id, id_in, id_out});
            return true;
        } catch (IOException ex) {
            JDebug.out.log(Level.SEVERE, "{0} error writing file #{1}!", new Object[]{queueName, id});
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }
    }       
    
    private boolean readFile(int id) {
        try (FileInputStream fileInDat = new FileInputStream(getFileName(id));
                ObjectInputStream objInDat = new ObjectInputStream(fileInDat)) {
            lastPeekId = id;
            lastPeekBlock = (DataList) objInDat.readObject();
            JDebug.out.log(Level.FINE, "{0} file #{1} read (id_in={2} id_out={3})", new Object[]{queueName, id, id_in, id_out});
            return true;
        } catch (IOException | ClassNotFoundException ex) {
            JDebug.out.log(Level.SEVERE, "{0} error reading file #{1}!", id);
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }            
    }    
    
    private void deleteFile(int id) {
        (new File(getFileName(id))).delete();
        JDebug.out.log(Level.FINE, "{0} file #{1} deleted (id_in={2} id_out={3})", new Object[]{queueName, id, id_in, id_out});
    }    
}
