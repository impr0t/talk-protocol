package shared;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class Message {
	private String sequenceNumber;
	private String type;
	private String source;
	private String destination;
	private byte[] payload;
	
	private Message(String sequenceNumber, String type, String source, String destination, byte[] payload) {
		
		if (type.length() > Constants.MAX_TYPE_LENGTH)
			throw new IllegalArgumentException("Type length is out of range.");
		
		if (source.length() > Constants.MAX_SRC_LENGTH)
			throw new IllegalArgumentException("Source length is out of range.");
		
		if (destination.length() > Constants.MAX_DEST_LENGTH)
			throw new IllegalArgumentException("Destination length is out of range.");
		
		this.sequenceNumber = sequenceNumber;
		this.type = type;
		this.source = source;
		this.destination = destination;
		this.payload = payload;
	}
	
	public Message(String type, String source, String destination, byte[] payload) {
		// call the private constructor.
		this(buildSequence(), type, source, destination, payload);
	}
	
	public static byte[] toBytes(Message message) throws UnsupportedEncodingException {
		
		byte[] sequence = getBytes(message.sequenceNumber);
		
		byte[] type = getBytes(message.type);
		
		byte[] source = getBytes(message.source);
		
		byte[] dest = getBytes(message.destination);
		
		int resultLength = Constants.MAX_SEQ_LENGTH + 
							Constants.MAX_TYPE_LENGTH + 
							Constants.MAX_SRC_LENGTH + 
							Constants.MAX_DEST_LENGTH + 
							message.payload.length;
		
		byte[] result = new byte[resultLength];
		
		int pos = 0;
		// copy the sequence in the result array.
		System.arraycopy(sequence, 0, result, pos, sequence.length);
		
		// copy the type into the result array.
		pos = Constants.MAX_SEQ_LENGTH;
		System.arraycopy(type, 0, result, pos, type.length);
		
		// copy the source into the result array.
		pos += Constants.MAX_TYPE_LENGTH;
		System.arraycopy(source, 0, result, pos, source.length);
		
		// copy the destination into the result array.
		pos += Constants.MAX_SRC_LENGTH;
		System.arraycopy(dest, 0, result, pos, dest.length);
		
		// copy the pay load into the byte result array.
		pos += Constants.MAX_DEST_LENGTH;
		System.arraycopy(message.payload, 0, result, pos, message.payload.length);
		
		return result;
	}
	
	public static Message fromBytes(byte[] message) {
		int offset = 0;
		String seq = new String(getField(message,offset, Constants.MAX_SEQ_LENGTH));		
		
		offset += Constants.MAX_SEQ_LENGTH;
		String type = new String(getField(message, offset, Constants.MAX_TYPE_LENGTH));
		
		offset += Constants.MAX_TYPE_LENGTH;
		String src = new String(getField(message, offset, Constants.MAX_SRC_LENGTH));
		
		offset += Constants.MAX_SRC_LENGTH;
		String dest = new String(getField(message, offset, Constants.MAX_DEST_LENGTH));
		
		offset += Constants.MAX_DEST_LENGTH;
		byte[] payload = getField(message, offset, message.length - offset);
		
		return new Message(seq, type, src, dest, payload);
	}
	
	private static byte[] getBytes(String s) throws UnsupportedEncodingException {
		return s.getBytes("ASCII");
	}
	
	private static byte[] getField(byte[] message, int offset, int length) {
		ByteBuffer b = ByteBuffer.wrap(message, offset, length);
		byte[] result = new byte[length];
		b.get(result, 0, length);
		return result;
	}
	
	private static String buildSequence() {
		// date format as year month day hour minute second millisecond.
		SimpleDateFormat dateFormatGMT = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT")); // set to Greenwich mean time.
		return dateFormatGMT.format(new Date());
	}
	
	public String getSequence() {
		return this.sequenceNumber.trim();
	}
		
	public String getSource() {
		return this.source.trim();
	}
	
	public void setSource(String source) {
		this.source = source.trim();
	}
	
	public String getDestination() {
		return this.destination.trim();
	}
	
	public void setDestination(String destination) {
		this.destination = destination.trim();
	}
	
	public String getType() {
		return this.type.trim();
	}
	
	public void setType(String type) {
		this.type = type.trim();
	}
	
	public byte[] getPayload() {
		return this.payload;
	}
	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	@Override
	public String toString() {
		return "Message [sequenceNumber=" + sequenceNumber + ", type=" + type + ", source=" + source + ", destination="
				+ destination + ", payload=" + Arrays.toString(payload) + "]";
	}
}
