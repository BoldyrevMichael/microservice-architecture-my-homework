# delivery-service

**Назначение.** Курьеры точки, назначение на слот времени, статус доставки.

**Зона ответственности.**
- Входит: курьеры точки и их слоты, назначение курьера на заказ, маршрут курьеру,
  отметка вручения и оплаты курьеру.
- Не входит: зона доставки (shop-service), построение маршрута (geo-service).

**Зависимости.** geo-service — время в пути до адреса.

**Запросы и команды.**

| Операция | Вызов |
|---|---|
| Курьеры точки (владелец) | `GET /api/v1/shops/{shopId}/couriers`, `POST /api/v1/shops/{shopId}/couriers` |
| Мои доставки (курьер) | `GET /api/v1/deliveries?courierId=me` |
| Доставка заказа | `GET /api/v1/deliveries/{orderId}` |
| Забрал на точке | `POST /api/v1/deliveries/{orderId}/picked-up` |
| Вручил | `POST /api/v1/deliveries/{orderId}/completed` `{paidOnDelivery}` |

**Публикуемые события.** `DeliveryAssigned`, `DeliveryCompleted`.

**Подписки на события.** `OrderPlaced` с доставкой — резерв курьера на слот;
`OrderReady` — отправка курьера; `OrderCancelled` — освобождение слота.

**Данные.** Курьеры, слоты, доставки.
