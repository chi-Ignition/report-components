package com.chitek.ignition.report.gateway;

import com.chitek.ignition.report.shapes.StatusChart;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.rm.archiver.RMArchiver;

public class GatewayHook extends AbstractGatewayModuleHook {
    GatewayContext context;

    @Override
    public void setup(GatewayContext context) {
        this.context = context;
    }

    @Override
    public void startup(LicenseState activationState) {
        // shape classes need to be registered from the gateway and designer hooks
        RMArchiver.registerClass(StatusChart.ARCHIVE_NAME, StatusChart.class);
    }

    @Override
    public void shutdown() {

    }
    
    public boolean isFreeModule() {
    	return true;
    }
}
