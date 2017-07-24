/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

/**
 *
 * @author vogler
 */
public class CacheItem {
    public int threadNr = -1;
    public long timeMS = -1L;
    public int nanos = -1;
    public int addNanos = 0;
}
