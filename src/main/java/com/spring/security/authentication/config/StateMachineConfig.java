package com.spring.security.authentication.config;


import com.spring.security.authentication.enums.OrderEvent;
import com.spring.security.authentication.enums.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
            throws Exception {
        states
                .withStates()
                .initial(OrderState.PENDING)
                .states(EnumSet.allOf(OrderState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {
        transitions
                .withExternal().source(OrderState.PENDING).target(OrderState.PAID).event(OrderEvent.PAY).and()
                .withExternal().source(OrderState.PAID).target(OrderState.SHIPPED).event(OrderEvent.SHIP).and()
                .withExternal().source(OrderState.PAID).target(OrderState.REFUNDING).event(OrderEvent.APPLY_REFUND).and()
                .withExternal().source(OrderState.REFUNDING).target(OrderState.REFUNDED).event(OrderEvent.CONFIRM_REFUND).and()
                .withExternal().source(OrderState.SHIPPED).target(OrderState.COMPLETED).event(OrderEvent.COMPLETE);
    }

    @Bean
    public StateMachineListener<OrderState, OrderEvent> listener() {
        return new StateMachineListenerAdapter<OrderState, OrderEvent>() {
            @Override
            public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }
}
