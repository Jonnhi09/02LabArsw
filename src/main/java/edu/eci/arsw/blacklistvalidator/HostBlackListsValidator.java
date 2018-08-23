/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    public static final int BLACK_LIST_ALARM_COUNT = 5;

    /**
     * Check the given host's IP address in all the available black lists, and
     * report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case. The
     * search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as NOT
     * Trustworthy, and the list of the five blacklists returned.
     *
     * @param ipaddress suspicious host's IP address.
     * @param numThreads number of threads.
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int numThreads) throws InterruptedException {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        LinkedList<SearchThread> threadList = new LinkedList<>();

        AtomicInteger ocurrencesCount = new AtomicInteger(0);
        int checkedListsCount = 0;

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        //Variables para asignar los rangos dependiendo del numero de hilos.
        int numServers = skds.getRegisteredServersCount();
        int div = numServers / numThreads;
        int mod = numServers % numThreads;
        int minRange = 0;
        int maxRange = minRange + div;
        int var = maxRange + mod;
        // Crear un hilo con su respectivo segmento de busqueda.
        while (var <= numServers) {
            if (var == numServers) {
                threadList.add(new SearchThread(minRange, var, ipaddress, ocurrencesCount));
            } else {
                threadList.add(new SearchThread(minRange, maxRange, ipaddress, ocurrencesCount));
            }
            minRange = maxRange;
            maxRange = minRange + div;
            var = maxRange + mod;
        }

        for (SearchThread i : threadList) {
            i.start();
        }
        
        for (SearchThread j : threadList) {
            j.join();
        }
        
        for (SearchThread k : threadList) {
            blackListOcurrences.addAll(k.getBlackListOcurrences());
            checkedListsCount+=k.getCheckedListsCount();
        }

        if (ocurrencesCount.get() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}
