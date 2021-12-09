package com.github.fenrir.xlocalmonitor.monitors.netdata;

import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataCpuUtilMetric;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.Value;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Monitor(name = "NetdataCpuGuestUtilMonitor", streams = {"system.cpu.guest.util"},
         inspectors = "netdata")
public class NetdataCpuGuestUtilMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "NetdataCpuGuestUtilMonitor");

    public static String getMonitorName(){
        return "NetdataCpuGuestUtilMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class NetdataCpuGuestUtilMonitorTimerTask extends TimerTask {

        @Getter @Setter private NetdataCpuGuestUtilMonitor monitor;
        @Getter @Setter private NetdataAPI api;

        public NetdataCpuGuestUtilMonitorTimerTask(NetdataCpuGuestUtilMonitor monitor,
                                                   NetdataAPI api){
            this.setMonitor(monitor);
            this.setApi(api);
        }
        @Override
        public void run() {
            NetdataCpuUtilMetric metric = this.getApi().getCpuUtil();
            Value<Double> value = metric.getLatestMetric(NetdataCpuUtilMetric.Kind.GUEST, Double.class);
            this.getMonitor().pushStreamData(
                    "system.cpu.guest.util",
                    this.getMonitor().createStreamData(
                    "system.cpu.guest.util",
                            this.getMonitor().getHostname(), value.getValue(), value.getTimestamp()
                    )
            );
        }
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    @Override
    protected void postStart() {
        logger.info("monitor stop running ... ");
    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new NetdataCpuGuestUtilMonitorTimerTask(
                this, (NetdataAPI) this.getApiMap().get("netdata")
        ), (long) 1000);

        lock.lock();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void preStart() {
        // registerStream("system.cpu.guest.util");
        logger.info("monitor start running ... ");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}