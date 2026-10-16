# order-service

**Назначение.** Оформление заказа и его жизненный цикл: расчёт стоимости, резерв,
платёж, время выдачи, маршрут, статус.

**Зона ответственности.**
- Входит: состав и сумма заказа, способ получения и оплаты, время выдачи, статус,
  отмена, история заказов клиента.
- Не входит: приготовление (kitchen-service), оплата (billing-service), доставка
  (delivery-service).

**Зависимости.** shop-service, catalog-service, promotion-service, inventory-service,
billing-service, kitchen-service, geo-service — синхронно при оформлении.

**Запросы и команды.**

| Операция | Вызов |
|---|---|
| Оформить заказ | `POST /api/v1/orders` `{shopId, items[{itemId, qty}], fulfillment: PICKUP / DELIVERY, addressId, paymentMethod: ONLINE / ON_SITE, customerLocation}` → `{orderId, amount, readyAt, route}` |
| Заказ | `GET /api/v1/orders/{id}` |
| Мои заказы | `GET /api/v1/orders` |
| Отменить | `POST /api/v1/orders/{id}/cancel` |
| Заказ от имени точки (сотрудник, факс) | `POST /api/v1/orders` с `X-Shop-Id` без клиента |

Статусы: `PLACED → READY → COMPLETED`, из `PLACED` возможна `CANCELLED`.

**Публикуемые события.** `OrderPlaced`, `OrderCancelled`.

**Подписки на события.** `PaymentConfirmed`, `PaymentFailed`, `OrderReady`,
`OrderHandedOver`, `DeliveryAssigned`, `DeliveryCompleted` — обновление статуса.

**Данные.** Заказы, позиции, снимок цен и скидок, статусы.
