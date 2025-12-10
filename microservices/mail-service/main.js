import amqp from "amqplib";

const RABBIT_URL = "amqp://localhost:5672";
const QUEUE_NAME = "email.queue";
const EXCHANGE_NAME = "email.exchange";
const ROUTING_KEY = "email.send";

async function start() {
  try {
    console.log("[*] Connecting to RabbitMQ...");

    const connection = await amqp.connect(RABBIT_URL);
    const channel = await connection.createChannel();

    await channel.assertExchange(EXCHANGE_NAME, "topic", { durable: true });
    await channel.assertQueue(QUEUE_NAME, { durable: true });
    await channel.bindQueue(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

    console.log("[*] Waiting for messages in:", QUEUE_NAME);

    channel.consume(
      QUEUE_NAME,
      (msg) => {
        if (msg) {
          const content = msg.content.toString();
          console.log("📩 Received message:", content);

          channel.ack(msg);
        }
      },
      { noAck: false }
    );
  } catch (err) {
    console.error("❌ Error:", err);
    process.exit(1);
  }
}

start();
