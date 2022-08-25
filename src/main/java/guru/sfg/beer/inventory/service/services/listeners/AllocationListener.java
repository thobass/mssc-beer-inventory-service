package guru.sfg.beer.inventory.service.services.listeners;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.services.AllocationService;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationListener {

    private final JmsTemplate jmsTemplate;
    private final AllocationService allocationService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest) {
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
        builder.beerOrderDto(allocateOrderRequest.getBeerOrderDto());

        try {
            Boolean allocationResult = allocationService.allocateOrder(allocateOrderRequest.getBeerOrderDto());

            if (allocationResult) {
                builder.pendingInventory(false);
            } else {
                builder.pendingInventory(true);
            }
        } catch (Exception e) {
            log.error("Allocation failed for Order Id:" + allocateOrderRequest.getBeerOrderDto().getId());
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE,
                builder.build());
    }
}
