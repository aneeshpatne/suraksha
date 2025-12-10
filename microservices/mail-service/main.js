import amqp from "amqplib";
import nodemailer from "nodemailer";
import dotenv from "dotenv";

dotenv.config();

const RABBIT_URL = process.env.RABBIT_URL || "amqp://localhost:5672";
const QUEUE_NAME = "email.queue";
const EXCHANGE_NAME = "email.exchange";
const ROUTING_KEY = "email.send";

// Create nodemailer transporter
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: parseInt(process.env.SMTP_PORT),
  secure: false, // use STARTTLS
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS,
  },
});

async function sendMail(mailData) {
  try {
    const info = await transporter.sendMail({
      from: process.env.SMTP_FROM,
      to: mailData.to,
      subject: mailData.subject,
      html: mailData.body,
    });
    console.log("✅ Email sent:", info.messageId);
    return true;
  } catch (err) {
    console.error("❌ Failed to send email:", err);
    return false;
  }
}

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
      async (msg) => {
        if (msg) {
          const content = msg.content.toString();
          console.log("📩 Received message:", content);

          try {
            const mailData = JSON.parse(content);
            const success = await sendMail(mailData);

            if (success) {
              channel.ack(msg);
            } else {
              // Requeue the message on failure
              channel.nack(msg, false, true);
            }
          } catch (parseErr) {
            console.error("❌ Failed to parse message:", parseErr);
            channel.ack(msg); // Acknowledge invalid messages to prevent infinite loop
          }
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
