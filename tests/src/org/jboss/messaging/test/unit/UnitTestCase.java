/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.messaging.test.unit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.messaging.core.message.Message;
import org.jboss.messaging.core.message.MessageReference;
import org.jboss.messaging.core.message.impl.MessageImpl;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.transaction.impl.XidImpl;

/**
 * 
 * Helper base class for our unit tests
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class UnitTestCase extends TestCase
{
   protected void assertByteArraysEquivalent(byte[] bytes1, byte[] bytes2)
   {
      if (bytes1.length != bytes2.length)
      {
         fail("Byte arrays different sizes bytes1: " + dumpBytes(bytes1) + " bytes2: " + dumpBytes(bytes2)); 
      }
      
      for (int i = 0; i < bytes1.length; i++)
      {
         if (bytes1[i] != bytes2[i])
         {
            fail("Byte arrays not equivalent: " + dumpBytes(bytes1) + " bytes2: " + dumpBytes(bytes2)); 
         }
      }
   }
   
   protected String dumpBytes(byte[] bytes)
   {
      StringBuffer buff = new StringBuffer();
      
      buff.append(System.identityHashCode(bytes) + ", size: " + bytes.length + " [");
      
      for (int i = 0; i < bytes.length; i++)
      {
         buff.append(bytes[i]);
         
         if (i != bytes.length - 1)
         {
            buff.append(", ");
         }
      }
      
      buff.append("]");
      
      return buff.toString();      
   }
   
   protected boolean deleteDirectory(File directory)
   {
      if (directory.isDirectory())
      {
         String[] files = directory.list();

         for (int j = 0; j < files.length; j++)
         {
            if (!deleteDirectory(new File(directory, files[j])))
            {
               return false;
            }
         }
      }

      return directory.delete();
   }
   
   protected void copyRecursive(File from , File to) throws Exception
   {     
       if (from.isDirectory())
       {
           if (!to.exists())
           {
               to.mkdir();
           }
           
           String[] subs = from.list();
           
           for (int i = 0; i < subs.length; i++)
           {
               copyRecursive(new File(from, subs[i]),
                             new File(to, subs[i]));
           }
       }
       else
       {           
           InputStream in = null;
           
           OutputStream out = null;
                      
           try
           {           
              in = new BufferedInputStream(new FileInputStream(from));              
              
              out = new BufferedOutputStream(new FileOutputStream(to));
              
              int b;
              
              while ((b = in.read()) != -1)
              {
                  out.write(b);
              }
           }
           finally
           {   
              if (in != null)
              {
                 in.close();
              }
              
              if (out != null)
              {
                 out.close();
              }
           }
       }
   }
   
   protected void assertRefListsIdenticalRefs(List<MessageReference> l1, List<MessageReference> l2)
   {
      if (l1.size() != l2.size())
      {
         fail("Lists different sizes: " + l1.size() + ", " + l2.size());
      }
      
      Iterator<MessageReference> iter1 = l1.iterator();
      Iterator<MessageReference> iter2 = l2.iterator();
      
      while (iter1.hasNext())
      {
         MessageReference o1 = iter1.next();
         MessageReference o2 = iter2.next();
                  
         assertTrue(o1 == o2);
      }                   
   }
   
   protected void assertRefListsEquivalent(List<MessageReference> l1, List<MessageReference> l2)
   {
      if (l1.size() != l2.size())
      {
         fail("Lists different sizes: " + l1.size() + ", " + l2.size());
      }
      
      Iterator<MessageReference> iter1 = l1.iterator();
      Iterator<MessageReference> iter2 = l2.iterator();
      
      while (iter1.hasNext())
      {
         MessageReference o1 = iter1.next();
         MessageReference o2 = iter2.next();
                  
         assertEquals(o1.getMessage().getMessageID(), o2.getMessage().getMessageID());
         
         assertEquals(o1.getScheduledDeliveryTime(), o2.getScheduledDeliveryTime());
         
         assertEquals(o1.getDeliveryCount(), o2.getDeliveryCount());
      }                   
   }
         
   protected Message generateMessage(long id)
   {
      Message message = new MessageImpl(0, true, 0, System.currentTimeMillis(), (byte)4);
      
      message.setMessageID(id);
      
      byte[] bytes = new byte[1024];
      
      for (int i = 0; i < 1024; i++)
      {
         bytes[i] = (byte)i;
      }
      
      message.setPayload(bytes);
      
      return message;
   }
   
   protected MessageReference generateReference(Queue queue, long id)
   {
      Message message = generateMessage(id);
      
      return message.createReference(queue);
   }
   
   protected void assertEquivalent(Message msg1, Message msg2)
   {
      assertEquivalent(msg1, msg2, true);
   }
   
   protected void assertEquivalent(Message msg1, Message msg2, boolean exactQueue)
   {
      assertEquals(msg1.getMessageID(), msg2.getMessageID());
      
      assertEquals(msg1.isDurable(), msg2.isDurable());
      
      assertEquals(msg1.getExpiration(), msg2.getExpiration());
      
      assertEquals(msg1.getTimestamp(), msg2.getTimestamp());
      
      assertEquals(msg1.getPriority(), msg2.getPriority());
      
      assertEquals(msg1.getType(), msg2.getType());         
      
      if (msg1.getPayload() == null)
      {
         assertNull(msg2.getPayload());
      }
      else
      {
         assertByteArraysEquivalent(msg1.getPayload(), msg2.getPayload());
      }     
      
      assertMapsEquivalent(msg1.getHeaders(), msg2.getHeaders());
      
      assertEquals(msg1.getDurableRefCount(), msg2.getDurableRefCount());           
   }
   
   protected void assertMapsEquivalent(Map<String, Object> headers1, Map<String, Object> headers2)
   {
      assertEquals(headers1.size(), headers2.size());
      
      for (Map.Entry<String, Object> entry : headers1.entrySet())
      {
         assertEquals(entry.getValue(), headers2.get(entry.getKey()));
      }
   }
   
   protected XidImpl generateXid()
   {      
      String id1 = java.util.UUID.randomUUID().toString();
      
      String id2 = java.util.UUID.randomUUID().toString();
      
      int id = (int)(Math.random() * 100);
      
      XidImpl xid = new XidImpl(id1.getBytes(), id, id2.getBytes());
      
      return xid;
   }
   

}
