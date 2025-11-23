/*
 * Copyright 2025 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.uidgen.service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import l9g.uidgen.handler.LdapHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@Service
public class UidgenService
{
  private final boolean[] uidArray;

  private final String outputFormat;
  private final String uniqueTag;

  private final Random random = new Random(System.currentTimeMillis());

  private final int maxNumberOfUids;
  
  private final LdapHandler ldapHandler;

  @Getter
  private int availableUids;

  public UidgenService(
    @Value("${uidgen.number-of-digits}") int numberOfDigits,
    @Value("${uidgen.unique-tag}") String uniqueTag,
    LdapHandler ldapHandler
  )
  {
    log.debug("numberOfDigits={}", numberOfDigits);
    maxNumberOfUids = (int)(Math.pow(10.0, (double)numberOfDigits));
    log.debug("maxNumberOfUids={}", maxNumberOfUids);
    this.uidArray = new boolean[maxNumberOfUids];
    this.uniqueTag = uniqueTag;
    this.outputFormat = String.format("%s%%0%dd", uniqueTag, numberOfDigits);
    log.debug("outputFormat={}", outputFormat);
    this.availableUids = maxNumberOfUids;
    this.ldapHandler = ldapHandler;
  }

  @PostConstruct
  public synchronized void initialize() throws Throwable
  {
    log.debug("initialize");

    this.availableUids = maxNumberOfUids;
    Arrays.fill(uidArray, false);
    
    int startIndex = uniqueTag.length();
    ldapHandler.readAllLdapEntries();
    ldapHandler.getLdapEntryMap().forEach((key,value) -> {
      int uidIndex = (Integer.parseInt(key.substring(startIndex)));
      uidArray[uidIndex] = true;
      availableUids--;
    });
    log.debug("availableUids: {}", availableUids);
  }

  public synchronized List<String> findUids(int numberOfUids)
  {
    List<String> uidsList = new ArrayList<>();

    String uid;

    for(int i = 0; i < numberOfUids && (uid = findNextUid()) != null; i ++)
    {
      uidsList.add(uid);
    }

    return uidsList;
  }

  private String findNextUid()
  {
    String uid = null;
    
    if(availableUids > 0)
    {
      int index = random.nextInt(maxNumberOfUids);

      while(uidArray[index] == true )
      {
        index++;
        if ( index == maxNumberOfUids)
        {
          index = 0;
        }
      }

      if( ! uidArray[index])
      {
        uidArray[index] = true;
        availableUids --;
        uid = String.format(outputFormat, index);
      }
    }
    
    return uid;
  }

}
