package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.schema.TestUtil;
import com.futurewei.alioth.controller.schema.Vpc;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MessageClientTest {

    @Test
    public void runConsumer() {
    }

    @Test
    public void runProducer() {
    }

    @Test
    public void basicE2EVerification() {
        final Vpc.VpcState vpc_state = GoalStateUtil.CreateVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final Vpc.VpcState vpc_state2 = GoalStateUtil.CreateVpcState(Common.OperationType.UPDATE,
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "MiniVpc",
                "192.168.1.0/29");

        GoalState goalstate = GoalState.newBuilder()
                .addVpcStates(vpc_state).addVpcStates(vpc_state2)
                .build();

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        client.runProducer("controller_test", goalstate);
        List goalStateList = client.runConsumer("controller_test", true);

        Assert.assertEquals("invalid message count", 1, goalStateList.size());
        GoalState receivedGoalState = (GoalState) goalStateList.get(0);

        Assert.assertEquals("invalid vpc state count", 2, receivedGoalState.getVpcStatesCount());
        Assert.assertEquals("invalid subnet state count", 0, receivedGoalState.getSubnetStatesCount());
        Assert.assertEquals("invalid port state count", 0, receivedGoalState.getPortStatesCount());
        Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

        TestUtil.AssertVpcStates(vpc_state, receivedGoalState.getVpcStates(0));
        TestUtil.AssertVpcStates(vpc_state2, receivedGoalState.getVpcStates(1));

        try {

            TestUtil.AssertVpcStates(vpc_state, receivedGoalState.getVpcStates(1));
            Assert.assertTrue(false);
        } catch (AssertionError assertionError){
            //catch expected exception
            Assert.assertTrue(true);
        }

    }
}