package at.rocworks.oa4j.logger.data.lists;

import at.rocworks.oa4j.logger.data.base.DataItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DataList implements Serializable, IDataListMutable {

    //private static final long serialVersionUID = 1L;
    private final DataItem[] items;
    private int highWaterMark;

    public DataList(int size) {
        highWaterMark = -1;
        items = (size > 0 ? new DataItem[size] : new DataItem[1]);
    }

    @Override
    public int size() {
        return items != null ? items.length : 0;
    }

    @Override
    public DataItem getItem(int idx) {
        return items[idx];
    }

    @Override
    public void setItem(int idx, DataItem item) {
        if (idx == highWaterMark && item == null) {
            highWaterMark--;
        } else if (idx > highWaterMark && item != null) {
            highWaterMark = idx;
        }
        items[idx] = item;
    }

    @Override
    public void delItem(int idx) {
        if (idx == highWaterMark) {
            highWaterMark--;
        }
        items[idx] = null;
    }

    @Override
    public boolean addItem(DataItem item) {
        if (highWaterMark + 1 < items.length) {
            items[++highWaterMark] = item;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int addItems(IDataListImmutable e, int i) {
        DataItem item;
        while (highWaterMark + 1 < items.length && i <= e.getHighWaterMark() /*i < e.size()*/) {
            if ((item = e.getItem(i++)) != null) {
                items[++highWaterMark] = item;
            }
        }
        if (i <= e.getHighWaterMark() /*i < e.size()*/) {
            return i; // not all events added
        } else if (highWaterMark + 1 == items.length) {
            return -1; // all events added and block full
        } else {
            return 0; // all events added and block not full
        }
    }

    @Override
    public int getHighWaterMark() {
        return highWaterMark;
    }

    @Override
    public boolean isEmpty() {
        return highWaterMark == -1;
    }

    @Override
    public void clear() {
        highWaterMark = -1;
    }

    @Override
    public boolean isFull() {
        return highWaterMark == items.length - 1;
    }
    
    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
                ObjectOutput object = new ObjectOutputStream(data)) {
            object.writeObject(this);
            object.flush();
            return data.toByteArray();
        }              
    }
    
    public static DataList fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream data = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(data)) {
            Object object = in.readObject();
            if (object instanceof DataList) {
                return (DataList) object;                            
            } else {
                throw new ClassNotFoundException("class "+object.getClass().getName()+" is not an instance of "+DataList.class.getName());
            }
        }        
    }
}
