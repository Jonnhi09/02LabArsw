/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jonathan Prieto
 */
public class SearchThread extends Thread {

    private AtomicInteger ocurrences;
    private int min, max, checkedListsCount;
    private String ipAddress;
    private HostBlacklistsDataSourceFacade skds;
    private LinkedList<Integer> blackListOcurrences;

    public SearchThread(int min, int max, String ipAddress, AtomicInteger ocurrences) {
        this.min = min;
        this.max = max;
        this.ocurrences = ocurrences;
        this.checkedListsCount = 0;
        this.ipAddress = ipAddress;
        blackListOcurrences = new LinkedList<>();
        skds = HostBlacklistsDataSourceFacade.getInstance();
    }

    @Override
    public void run() {
        for (int i = min; i < max && ocurrences.get() < HostBlackListsValidator.BLACK_LIST_ALARM_COUNT; i++) {
            checkedListsCount++;
            if (skds.isInBlackListServer(i, ipAddress)) {
                blackListOcurrences.add(i);
                ocurrences.getAndIncrement();
            }
        }
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }

    public LinkedList<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

}
