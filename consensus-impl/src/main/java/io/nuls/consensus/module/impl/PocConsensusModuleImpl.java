package io.nuls.consensus.module.impl;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.genesis.DevGenesisBlock;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.entity.validator.block.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.handler.*;
import io.nuls.consensus.handler.filter.*;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleImpl extends AbstractConsensusModule {

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    private boolean delegatePeer = false;

    @Override
    public void start() {
        //todo 判断启动模式，选择创世块
        NulsContext.getInstance().setGenesisBlock(DevGenesisBlock.getInstance());
        delegatePeer = ConfigLoader.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        PocBlockValidatorManager.initBlockValidators();
        this.checkGenesisBlock();
        this.checkBlockHeight();
        this.checkConsensusStatus();
        this.startBlockMaintenanceThread();
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_RED_PUNISH, RedPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH, YellowPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_REGISTER_AGENT, RegisterAgentEvent.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
        this.registerHanders();
        this.registerService(PocConsensusServiceImpl.getInstance());
        Log.info("the POC consensus module is started!");

    }

    private void registerHanders() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        blockEventHandler.addFilter(new BlockEventFilter());
        processorService.registerEventHandler(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        blockHeaderHandler.addFilter(new BlockHeaderEventFilter());
        processorService.registerEventHandler(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        getBlockHandler.addFilter(new GetBlockEventFilter());
        processorService.registerEventHandler(GetBlockEvent.class, getBlockHandler);

        RegisterAgentHandler registerAgentHandler = new RegisterAgentHandler();
        registerAgentHandler.addFilter(new RegisterAgentEventFilter());
        processorService.registerEventHandler(RegisterAgentEvent.class, registerAgentHandler);

        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        joinConsensusHandler.addFilter(new JoinConsensusEventFilter());
        processorService.registerEventHandler(JoinConsensusEvent.class, joinConsensusHandler);

        ExitConsensusHandler exitConsensusHandler = new ExitConsensusHandler();
        exitConsensusHandler.addFilter(new ExitConsensusEventFilter());
        processorService.registerEventHandler(ExitConsensusEvent.class, exitConsensusHandler);

        RedPunishHandler redPunishHandler = new RedPunishHandler();
        redPunishHandler.addFilter(new RedPunishEventFilter());
        processorService.registerEventHandler(RedPunishConsensusEvent.class, redPunishHandler);

        YellowPunishHandler yellowPunishHandler = new YellowPunishHandler();
        yellowPunishHandler.addFilter(new YellowPunishEventFilter());
        processorService.registerEventHandler(YellowPunishConsensusEvent.class, yellowPunishHandler);
    }

    private void checkConsensusStatus() {
        //todo
//        cache consensus member

//        Judge own state
    }

    private void checkBlockHeight() {
        //todo
    }

    private void startBlockMaintenanceThread() {
        ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                "BlockMaintenance", BlockMaintenanceThread.getInstance());
    }


    /**
     * check genesis block
     * if genesis block isn't exist,create and download
     * exist: Verify it .
     */
    private void checkGenesisBlock() {
        //todo
    }

    @Override
    public void shutdown() {
        //todo

    }

    @Override
    public void destroy() {
        //todo
    }

    @Override
    public String getInfo() {
        //todo 加入共识时间、出块数量、收益金额。。。
        return null;
    }

    @Override
    public int getVersion() {
        return PocConsensusConstant.POC_CONSENSUS_MODULE_VERSION;
    }

    public boolean isDelegatePeer() {
        return delegatePeer;
    }
}