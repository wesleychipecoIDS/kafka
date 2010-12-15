/*
 * Copyright 2010 LinkedIn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.perf.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import kafka.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.producer.SimpleProducer;

/**
 * @author nnarkhed
 *
 */
public class TestProducer extends Thread implements Producer {

	private final SimpleProducer producer;
	private final String topic;
	private final int messageSize;
	private AtomicLong bytesSent =  new AtomicLong(0L);
	private AtomicLong messagesSent =  new AtomicLong(0L);
	private AtomicLong lastReportMessageSent = new AtomicLong(System.currentTimeMillis());
	private AtomicLong lastReportBytesSent = new AtomicLong(System.currentTimeMillis());
	private String procudername;
	private int batchSize;
	private int numParts;
	private boolean compression;  

	public TestProducer(String topic, String kafkaServerURL, int kafkaServerPort,
			int kafkaProducerBufferSize, int connectionTimeOut, int reconnectInterval,
			int messageSize, String name, int batchSize, int numParts, boolean compression)
	{
		producer = new SimpleProducer(kafkaServerURL,
				kafkaServerPort,
				kafkaProducerBufferSize,
				connectionTimeOut,
				reconnectInterval);
		this.topic = topic; 
		this.messageSize = messageSize;
		procudername = name;
		this.batchSize = batchSize;
		this.numParts = numParts;
		this.compression = compression;

	}

	public void run() {
		Random random = new Random();
		while(true)
		{
			List<Message> messageList = new ArrayList<Message>();
			for(int i = 0; i < batchSize; i++)
			{
				Message message = new Message(new byte[messageSize]);
				messageList.add(message);
			}
			ByteBufferMessageSet set = new ByteBufferMessageSet(messageList, compression);
			producer.send(topic, random.nextInt(numParts), set);
			bytesSent.getAndAdd(batchSize * messageSize);
			messagesSent.getAndAdd(messageList.size());
		}
	}

	public double getMBytesSentPs() {
		double val = ((double)bytesSent.get() / (System.currentTimeMillis() - lastReportBytesSent.get())) / (1024*1024);
		return val * 1000;
	}

	public double getMessagesSentPs() {
		double val = (double)messagesSent.get() / (System.currentTimeMillis() - lastReportMessageSent.get());
		return val * 1000;
	}

	public String getProducerName() {
		return procudername;
	}

	public long getTotalBytesSent() {
		return bytesSent.get();
	}

}
