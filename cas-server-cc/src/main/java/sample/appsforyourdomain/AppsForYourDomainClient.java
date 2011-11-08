/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package sample.appsforyourdomain;

import com.google.gdata.client.appsforyourdomain.AppsForYourDomainQuery;
import com.google.gdata.client.appsforyourdomain.AppsGroupsService;
import com.google.gdata.client.appsforyourdomain.EmailListRecipientService;
import com.google.gdata.client.appsforyourdomain.EmailListService;
import com.google.gdata.client.appsforyourdomain.NicknameService;
import com.google.gdata.client.appsforyourdomain.UserService;
import sample.util.SimpleCommandLineParser;
import com.google.gdata.data.Link;
import com.google.gdata.data.appsforyourdomain.AppsForYourDomainErrorCode;
import com.google.gdata.data.appsforyourdomain.AppsForYourDomainException;
import com.google.gdata.data.appsforyourdomain.EmailList;
import com.google.gdata.data.appsforyourdomain.Login;
import com.google.gdata.data.appsforyourdomain.Name;
import com.google.gdata.data.appsforyourdomain.Nickname;
import com.google.gdata.data.appsforyourdomain.Quota;
import com.google.gdata.data.appsforyourdomain.generic.GenericEntry;
import com.google.gdata.data.appsforyourdomain.generic.GenericFeed;
import com.google.gdata.data.appsforyourdomain.provisioning.EmailListEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.EmailListFeed;
import com.google.gdata.data.appsforyourdomain.provisioning.EmailListRecipientEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.EmailListRecipientFeed;
import com.google.gdata.data.appsforyourdomain.provisioning.NicknameEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.NicknameFeed;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.UserFeed;
import com.google.gdata.data.extensions.Who;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the client library for the Google Apps for Your Domain GData API.
 * It shows how the AppsForYourDomainService can be used to manage users on a 
 * domain.  This class contains a number of methods which can be used to
 * create, update, retrieve, and delete entities such as users, email lists
 * and nicknames.  Also included is sample usage of the client library.   
 * 
 */
public class AppsForYourDomainClient {

  private static final Logger LOGGER = Logger.getLogger(
      AppsForYourDomainClient.class.getName());

  private static final String APPS_FEEDS_URL_BASE =
      "https://apps-apis.google.com/a/feeds/";

  protected static final String SERVICE_VERSION = "2.0";

  protected String domainUrlBase;

  protected EmailListRecipientService emailListRecipientService;
  protected EmailListService emailListService;
  protected NicknameService nicknameService;
  protected UserService userService;
  protected AppsGroupsService groupService;

  /**
   * Public getter for AppsGroupsService
   * @return the groupService
   */
  public AppsGroupsService getGroupService() {
    return groupService;
  }

  protected final String domain;

  protected AppsForYourDomainClient(String domain) {
    this.domain = domain;
    this.domainUrlBase = APPS_FEEDS_URL_BASE + domain + "/";
  }

  /**
   * Constructs an AppsForYourDomainClient for the given domain using the
   * given admin credentials.
   *
   * @param adminEmail An admin user's email address such as admin@domain.com
   * @param adminPassword The admin's password
   * @param domain The domain to administer
   */
  public AppsForYourDomainClient(String adminEmail, String adminPassword,
      String domain) throws Exception {
    this(domain);

    // Configure all of the different Provisioning services
    userService = new UserService(
        "gdata-sample-AppsForYourDomain-UserService");
    userService.setUserCredentials(adminEmail, adminPassword);

    nicknameService = new NicknameService(
        "gdata-sample-AppsForYourDomain-NicknameService");
    nicknameService.setUserCredentials(adminEmail, adminPassword);

    emailListService = new EmailListService(
        "gdata-sample-AppsForYourDomain-EmailListService");
    emailListService.setUserCredentials(adminEmail, adminPassword);

    emailListRecipientService = new EmailListRecipientService(
        "gdata-sample-AppsForYourDomain-EmailListRecipientService");
    emailListRecipientService.setUserCredentials(adminEmail, adminPassword);

    groupService = new AppsGroupsService(adminEmail, adminPassword, domain, 
        "gdata-sample-AppsForYourDomain-AppsGroupService");
  }

  /**
   * Driver for the sample.
   */
  public void run() throws Exception {
    String randomFactor =
        Integer.toString(1000 + (new Random()).nextInt(9000));

    // Create a new user.
    String username = "SusanJones-" + randomFactor;
    String givenName = "Susan";
    String familyName = "Jones";
    String password = "123$$abc";

    String testGroupName = "discuss_general";
    String testGroupId = "newgroup-" + randomFactor;
    String testGroupDescription = "Discuss";

    String memberUserName = "john.doe." + randomFactor;
    String memberFirstName = "John";
    String memberLastName = "Doe";
    String memberPassword = "123$$$abc";

    String ownerUserName = "jane.doe." + randomFactor;
    String ownerFirstName = "Jane";
    String ownerLastName = "Doe";
    String ownerPassword = "123$$$abc";

    UserEntry createdUserEntry =
        createUser(username, givenName, familyName, password);

    // Update the user's family name.
    String newFamilyName = "Smith";
    createdUserEntry.getName().setFamilyName(newFamilyName);
    UserEntry updatedUserEntry = updateUser(username, createdUserEntry);

    // Create a nickname for the user.
    String nickname0 = "Susy-" + randomFactor;
    NicknameEntry createdNicknameEntry0 = createNickname(username, nickname0);

    // Create another nickname for the user.
    String nickname1 = "Suse-" + randomFactor;
    NicknameEntry createdNicknameEntry1 = createNickname(username, nickname1);

    // Retrieve the nicknames for user.
    NicknameFeed retrievedNicknameFeed = retrieveNicknames(username);
    StringBuffer nicknames = new StringBuffer();
    Iterator<NicknameEntry> nicknameIterator =
        retrievedNicknameFeed.getEntries().iterator();
    while (nicknameIterator.hasNext()) {
      nicknames.append(nicknameIterator.next().getNickname().getName());
      if (nicknameIterator.hasNext()) {
        nicknames.append(", ");
      }
    }
    LOGGER.log(Level.INFO,
        "User '" + username + "' has the following nicknames: {" +
        nicknames.toString() + "}.");

    // Delete the nicknames.
    deleteNickname(nickname0);
    deleteNickname(nickname1);

    // Create an email list.
    String emailList = "Staff-" + randomFactor;
    EmailListEntry createdEmailListEntry = createEmailList(emailList);

    // Add the user to the email list.
    addRecipientToEmailList(username + "@" + domain, emailList);

    // Add an external email address to the list.
    addRecipientToEmailList("jane.doe@externaldomain.com", emailList);

    // Retrieve the email lists for which this user is subscribed.
    EmailListFeed retrievedEmailListFeed = retrieveEmailLists(username);
    StringBuffer emailLists = new StringBuffer();
    Iterator<EmailListEntry> emailListIterator =
        retrievedEmailListFeed.getEntries().iterator();
    while (emailListIterator.hasNext()) {
      emailLists.append(emailListIterator.next().getEmailList().getName());
      if (emailListIterator.hasNext()) {
        emailLists.append(", ");
      }
    }
    LOGGER.log(Level.INFO,
        "User '" + username + "' is in the following emailLists: {" +
        emailLists.toString() + "}.");
     //*/
    LOGGER.log(Level.INFO, "Creating users for groups sample run");
    createUser(memberUserName, memberFirstName, memberLastName, memberPassword);
    createUser(ownerUserName, ownerFirstName, ownerLastName, ownerPassword);

    GenericFeed groupsFeed = null;
    GenericEntry groupsEntry = null;
    Iterator<GenericEntry> groupsEntryIterator = null;

    LOGGER.log(Level.INFO, "Creating group: " + testGroupId);
    groupsEntry =
        groupService.createGroup(testGroupId, testGroupName,
            testGroupDescription, "");
    LOGGER.log(Level.INFO, "Group created with following properties:\n"
        + groupsEntry.getAllProperties());

    groupsEntry =
        groupService.addMemberToGroup(testGroupId, memberUserName);
    LOGGER.log(Level.INFO, "Added member: \n" + groupsEntry.getAllProperties());

    groupsEntry = groupService.addOwnerToGroup(testGroupId, ownerUserName);
    LOGGER.log(Level.INFO, "Added owner: \n" + groupsEntry.getAllProperties());

    groupsEntry =
        groupService.updateGroup(testGroupId, testGroupName,
            testGroupDescription + "Updated: ", "");
    LOGGER.log(Level.INFO, "Updated group description:\n"
        + groupsEntry.getAllProperties());

    groupsFeed = groupService.retrieveAllMembers(testGroupId);
    groupsEntryIterator = groupsFeed.getEntries().iterator();

    StringBuffer members = new StringBuffer();

    while (groupsEntryIterator.hasNext()) {
      members.append(groupsEntryIterator.next().getProperty(
          AppsGroupsService.APPS_PROP_GROUP_MEMBER_ID));
      if (groupsEntryIterator.hasNext()) {
        members.append(", ");
      }
    }
    LOGGER.log(Level.INFO, testGroupId + " has these members: "
        + members.toString());

    groupsFeed = groupService.retreiveGroupOwners(testGroupId);
    groupsEntryIterator = groupsFeed.getEntries().iterator();

    StringBuffer owners = new StringBuffer();
    while (groupsEntryIterator.hasNext()) {
      owners.append(groupsEntryIterator.next().getProperty(
          AppsGroupsService.APPS_PROP_GROUP_EMAIL));
      if (groupsEntryIterator.hasNext()) {
        owners.append(", ");
      }
    }

    LOGGER.log(Level.INFO, testGroupName + " has these owners: "
        + owners.toString());
    groupsFeed = groupService.retrieveAllGroups();
    groupsEntryIterator = groupsFeed.getEntries().iterator();

    StringBuffer groups = new StringBuffer();
    while (groupsEntryIterator.hasNext()) {
      groups.append(groupsEntryIterator.next().getProperty(
          AppsGroupsService.APPS_PROP_GROUP_ID));
      if (groupsEntryIterator.hasNext()) {
        groups.append(", ");
      }
    }
    LOGGER.log(Level.INFO, "Domain has these groups:\n" + groups.toString());

    groupsFeed = groupService.retrieveGroups(memberUserName, true);
    groupsEntryIterator = groupsFeed.getEntries().iterator();

    groups = new StringBuffer();
    while (groupsEntryIterator.hasNext()) {
      groups.append(groupsEntryIterator.next().getProperty(
          AppsGroupsService.APPS_PROP_GROUP_ID));
      if (groupsEntryIterator.hasNext()) {
        groups.append(", ");
      }
    }
    LOGGER.log(Level.INFO, memberUserName + " is subscribed to these groups:\n"
        + groups.toString());

    boolean isMember = groupService.isMember(testGroupId, memberUserName);
    LOGGER.log(Level.INFO, memberUserName + " is member of " + testGroupId
        + "?: " + isMember);

    boolean isOwner = groupService.isOwner(testGroupId, ownerUserName);
    LOGGER.log(Level.INFO, ownerUserName + " is owner of " + testGroupId
        + "?: " + isOwner);

    groupService.deleteGroup(testGroupId);
    deleteUser(memberUserName);
    deleteUser(ownerUserName);

    // Delete the email list.
    deleteEmailList(emailList);

    // Delete the user.
    deleteUser(username);

    // Deleting a non-existent user and then catching the Exception.
    String fakeUsername = "SusanJones-fake"; 
    try {
      deleteUser(fakeUsername);
    } catch (AppsForYourDomainException e) {
      if (e.getErrorCode() == AppsForYourDomainErrorCode.EntityDoesNotExist) {
        // Do some post-error processing or logging.
        LOGGER.log(Level.INFO, "Do some post-error processing or logging.");
      }
    }
  }

  /**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @return A UserEntry object of the newly created user.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry createUser(String username, String givenName,
      String familyName, String password) throws AppsForYourDomainException, 
      ServiceException, IOException {

    return createUser(username, givenName, familyName, password, null, null);
  }

  /**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @param quotaLimitInMb User's quota limit in megabytes.  This field is only
   * used for domains with custom quota limits.
   * @return A UserEntry object of the newly created user.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry createUser(String username, String givenName,
      String familyName, String password, Integer quotaLimitInMb)
      throws AppsForYourDomainException, ServiceException, IOException {

    return createUser(username, givenName, familyName, password, null, 
        quotaLimitInMb);
  }

  /**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @param passwordHashFunction The name of the hash function to hash the 
   * password
   * @return A UserEntry object of the newly created user.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry createUser(String username, String givenName,
      String familyName, String password, String passwordHashFunction)
      throws AppsForYourDomainException, ServiceException, IOException {

    return createUser(username, givenName, familyName, password,
        passwordHashFunction, null);
  }

  /**
   * Creates a new user with an email account.
   *
   * @param username The username of the new user.
   * @param givenName The given name for the new user.
   * @param familyName the family name for the new user.
   * @param password The password for the new user.
   * @param passwordHashFunction Specifies the hash format of the password
   * parameter
   * @param quotaLimitInMb User's quota limit in megabytes.  This field is only
   * used for domains with custom quota limits.
   * @return A UserEntry object of the newly created user.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry createUser(String username, String givenName,
      String familyName, String password, String passwordHashFunction,
      Integer quotaLimitInMb)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Creating user '" + username + "'. Given Name: '" + givenName +
        "' Family Name: '" + familyName +
        (passwordHashFunction != null 
            ? "' Hash Function: '" + passwordHashFunction : "") + 
        (quotaLimitInMb != null 
            ? "' Quota Limit: '" + quotaLimitInMb + "'." : "'.")
        );

    UserEntry entry = new UserEntry();
    Login login = new Login();
    login.setUserName(username);
    login.setPassword(password);
    if (passwordHashFunction != null) {
      login.setHashFunctionName(passwordHashFunction);
    }
    entry.addExtension(login);

    Name name = new Name();
    name.setGivenName(givenName);
    name.setFamilyName(familyName);
    entry.addExtension(name);

    if (quotaLimitInMb != null) {
      Quota quota = new Quota();
      quota.setLimit(quotaLimitInMb);
      entry.addExtension(quota);
    }

    URL insertUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION );
    return userService.insert(insertUrl, entry);
  }

  /**
   * Retrieves a user.
   * 
   * @param username The user you wish to retrieve.
   * @return A UserEntry object of the retrieved user. 
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry retrieveUser(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving user '" + username + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.getEntry(retrieveUrl, UserEntry.class);
  }

  /**
   * Retrieves all users in domain.  This method may be very slow for domains
   * with a large number of users.  Any changes to users, including creations
   * and deletions, which are made after this method is called may or may not be
   * included in the Feed which is returned.
   *
   * @return A UserFeed object of the retrieved users.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserFeed retrieveAllUsers()
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving all users.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/");
    UserFeed allUsers = new UserFeed();
    UserFeed currentPage;
    Link nextLink;

    do {
      currentPage = userService.getFeed(retrieveUrl, UserFeed.class);
      allUsers.getEntries().addAll(currentPage.getEntries());
      nextLink = currentPage.getLink(Link.Rel.NEXT, Link.Type.ATOM);
      if (nextLink != null) {
        retrieveUrl = new URL(nextLink.getHref());
      }
    } while (nextLink != null);

    return allUsers;
  }

  /**
   * Retrieves one page (100) of users in domain.  Any changes to users,
   * including creations and deletions, which are made after this method is
   * called may or may not be included in the Feed which is returned.  If the
   * optional startUsername parameter is specified, one page of users is
   * returned which have usernames at or after the startUsername as per ASCII
   * value ordering with case-insensitivity.  A value of null or empty string
   * indicates you want results from the beginning of the list.
   *
   * @param startUsername The starting point of the page (optional).
   * @return A UserFeed object of the retrieved users.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserFeed retrievePageOfUsers(String startUsername)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Retrieving one page of users"
        + (startUsername != null ? " starting at " + startUsername : "") + ".");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/");
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(retrieveUrl);
    query.setStartUsername(startUsername);
    return userService.query(query, UserFeed.class);
  }

  /**
   * Updates a user.
   *
   * @param username The user to update.
   * @param userEntry The updated UserEntry for the user.
   * @return A UserEntry object of the newly updated user. 
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public UserEntry updateUser(String username, UserEntry userEntry)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Updating user '" + username + "'.");

    URL updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Deletes a user.
   * 
   * @param username The user you wish to delete.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public void deleteUser(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Deleting user '" + username + "'.");

    URL deleteUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    userService.delete(deleteUrl);
  }

  /**
   * Suspends a user. Note that executing this method for a user who is already
   * suspended has no effect.
   * 
   * @param username The user you wish to suspend.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public UserEntry suspendUser(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Suspending user '" + username + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    UserEntry userEntry = userService.getEntry(retrieveUrl, UserEntry.class);
    userEntry.getLogin().setSuspended(true);

    URL updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Restores a user. Note that executing this method for a user who is not
   * suspended has no effect.
   * 
   * @param username The user you wish to restore.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public UserEntry restoreUser(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Restoring user '" + username + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    UserEntry userEntry = userService.getEntry(retrieveUrl, UserEntry.class);
    userEntry.getLogin().setSuspended(false);

    URL updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Set admin privilege for user. Note that executing this method for a user
   * who is already an admin has no effect.
   * 
   * @param username The user you wish to make an admin.
   * @throws AppsForYourDomainException If a Provisioning API specific error
   *         occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public UserEntry addAdminPrivilege(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Setting admin privileges for user '" + username + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    UserEntry userEntry = userService.getEntry(retrieveUrl, UserEntry.class);
    userEntry.getLogin().setAdmin(true);

    URL updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Remove admin privilege for user. Note that executing this method for a user
   * who is not an admin has no effect.
   * 
   * @param username The user you wish to remove admin privileges.
   * @throws AppsForYourDomainException If a Provisioning API specific error
   *         occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public UserEntry removeAdminPrivilege(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Removing admin privileges for user '" + username + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    UserEntry userEntry = userService.getEntry(retrieveUrl, UserEntry.class);
    userEntry.getLogin().setAdmin(false);

    URL updateUrl = new URL(domainUrlBase + "user/" + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Require a user to change password at next login. Note that executing this
   * method for a user who is already required to change password at next login
   * as no effect.
   * 
   * @param username The user who must change his or her password.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public UserEntry forceUserToChangePassword(String username)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Requiring " + username + " to change password at " +
        "next login.");

    URL retrieveUrl = new URL(domainUrlBase + "user/"
        + SERVICE_VERSION + "/" + username);
    UserEntry userEntry = userService.getEntry(retrieveUrl, UserEntry.class);
    userEntry.getLogin().setChangePasswordAtNextLogin(true);

    URL updateUrl = new URL(domainUrlBase + "user/"
        + SERVICE_VERSION + "/" + username);
    return userService.update(updateUrl, userEntry);
  }

  /**
   * Creates a nickname for the username.
   *
   * @param username The user for which we want to create a nickname.
   * @param nickname The nickname you wish to create.
   * @return A NicknameEntry object of the newly created nickname. 
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public NicknameEntry createNickname(String username, String nickname) 
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Creating nickname '" + nickname +
        "' for user '" + username + "'.");

    NicknameEntry entry = new NicknameEntry();
    Nickname nicknameExtension = new Nickname();
    nicknameExtension.setName(nickname);
    entry.addExtension(nicknameExtension);

    Login login = new Login();
    login.setUserName(username);
    entry.addExtension(login);

    URL insertUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION);
    return nicknameService.insert(insertUrl, entry);
  }

  /**
   * Retrieves a nickname.
   *
   * @param nickname The nickname you wish to retrieve.
   * @return A NicknameEntry object of the newly created nickname. 
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public NicknameEntry retrieveNickname(String nickname) throws AppsForYourDomainException,
      ServiceException, IOException {
    LOGGER.log(Level.INFO, "Retrieving nickname '" + nickname + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION + "/" + nickname);
    return nicknameService.getEntry(retrieveUrl, NicknameEntry.class);
  }

  /**
   * Retrieves all nicknames for the given username.
   * 
   * @param username The user for which you want all nicknames.
   * @return A NicknameFeed object with all the nicknames for the user.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   */
  public NicknameFeed retrieveNicknames(String username)
      throws AppsForYourDomainException, ServiceException, IOException {
    LOGGER.log(Level.INFO,
        "Retrieving nicknames for user '" + username + "'.");

    URL feedUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION);
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(feedUrl);
    query.setUsername(username);
    return nicknameService.query(query, NicknameFeed.class);
  }

  /**
   * Retrieves one page (100) of nicknames in domain.  Any changes to
   * nicknames, including creations and deletions, which are made after
   * this method is called may or may not be included in the Feed which is
   * returned.  If the optional startNickname parameter is specified, one page
   * of nicknames is returned which have names at or after startNickname as per
   * ASCII value ordering with case-insensitivity.  A value of null or empty
   * string indicates you want results from the beginning of the list.
   *
   * @param startNickname The starting point of the page (optional).
   * @return A NicknameFeed object of the retrieved nicknames.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public NicknameFeed retrievePageOfNicknames(String startNickname)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Retrieving one page of nicknames"
        + (startNickname != null ? " starting at " + startNickname : "") + ".");

    URL retrieveUrl = new URL(
        domainUrlBase + "nickname/" + SERVICE_VERSION + "/");
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(retrieveUrl);
    query.setStartNickname(startNickname);
    return nicknameService.query(query, NicknameFeed.class);
  }

  /**
   * Retrieves all nicknames in domain.  This method may be very slow for
   * domains with a large number of nicknames.  Any changes to nicknames,
   * including creations and deletions, which are made after this method is
   * called may or may not be included in the Feed which is returned.
   *
   * @return A NicknameFeed object of the retrieved nicknames.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public NicknameFeed retrieveAllNicknames()
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving all nicknames.");

    URL retrieveUrl = new URL(domainUrlBase + "nickname/"
        + SERVICE_VERSION + "/");
    NicknameFeed allNicknames = new NicknameFeed();
    NicknameFeed currentPage;
    Link nextLink;

    do {
      currentPage = nicknameService.getFeed(retrieveUrl, NicknameFeed.class);
      allNicknames.getEntries().addAll(currentPage.getEntries());
      nextLink = currentPage.getLink(Link.Rel.NEXT, Link.Type.ATOM);
      if (nextLink != null) {
        retrieveUrl = new URL(nextLink.getHref());
      }
    } while (nextLink != null);

    return allNicknames;
  }

  /**
   * Deletes a nickname.
   *
   * @param nickname The nickname you wish to delete.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   * service.
   */
  public void deleteNickname(String nickname)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Deleting nickname '" + nickname + "'.");

    URL deleteUrl = new URL(domainUrlBase + "nickname/" + SERVICE_VERSION + "/" + nickname);
    nicknameService.delete(deleteUrl);
  }

  /**
   * Creates an empty email list.
   *
   * @param emailList The name of the email list you wish to create.
   * @return An EmailListEntry object of the newly created email list.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#createGroup(String,String,String,String)} 
   *             with Groups instead.
   */
  @Deprecated
  public EmailListEntry createEmailList(String emailList) 
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Creating email list '" + emailList + "'.");

    EmailListEntry entry = new EmailListEntry();
    EmailList emailListExtension = new EmailList();
    emailListExtension.setName(emailList);
    entry.addExtension(emailListExtension);

    URL insertUrl = new URL(domainUrlBase + "emailList/" + SERVICE_VERSION);
    return emailListService.insert(insertUrl, entry);
  }

  /**
   * Retrieves all email lists in which the recipient is subscribed. Recipient
   * can be given as a username or an email address of a hosted user.
   *
   * @param recipient The email address or username of the recipient.
   * @return An EmailListFeed object containing the email lists.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrieveGroups(String,boolean)} 
   *             with Groups.instead
   */
  @Deprecated
  public EmailListFeed retrieveEmailLists(String recipient)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving email lists for '" + recipient + "'.");

    URL feedUrl = new URL(domainUrlBase + "emailList/"
        + SERVICE_VERSION);
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(feedUrl);
    query.setRecipient(recipient);

    return emailListService.query(query, EmailListFeed.class);
  }

  /**
   * Retrieves all email lists in domain. This method may be very slow for
   * domains with a large number of email lists. Any changes to email lists,
   * including creations and deletions, which are made after this method is
   * called may or may not be included in the Feed which is returned.
   *
   * @return A EmailListFeed object of the retrieved email lists.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrieveAllGroups()}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListFeed retrieveAllEmailLists()
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving all email lists.");

    URL retrieveUrl = new URL(domainUrlBase + "emailList/"
        + SERVICE_VERSION + "/");
    EmailListFeed allEmailLists = new EmailListFeed();
    EmailListFeed currentPage;
    Link nextLink;

    do {
      currentPage = emailListService.getFeed(retrieveUrl, EmailListFeed.class);
      allEmailLists.getEntries().addAll(currentPage.getEntries());
      nextLink = currentPage.getLink(Link.Rel.NEXT, Link.Type.ATOM);
      if (nextLink != null) {
        retrieveUrl = new URL(nextLink.getHref());
      }
    } while (nextLink != null);

    return allEmailLists;
  }

  /**
   * Retrieves one page (100) of email lists in domain. Any changes to email
   * lists, including creations and deletions, which are made after this method
   * is called may or may not be included in the Feed which is returned. If the
   * optional startEmailListName parameter is specified, one page of email lists
   * is returned which have names at or after startEmailListName as per ASCII
   * value ordering with case-insensitivity. A value of null or empty string
   * indicates you want results from the beginning of the list.
   *
   * @param startEmailListName The starting point of the page (optional).
   * @return A EmailListFeed object of the retrieved email lists.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrievePageOfGroups(Link)}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListFeed retrievePageOfEmailLists(String startEmailListName)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Retrieving one page of email lists"
        + (startEmailListName != null ? " starting at " + startEmailListName : "") + ".");

    URL retrieveUrl = new URL(
          domainUrlBase + "emailList/" + SERVICE_VERSION + "/");
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(retrieveUrl);
    query.setStartEmailListName(startEmailListName);
    return emailListService.query(query, EmailListFeed.class);
  }

  /**
   * Retrieves an email list.
   *
   * @param emailList The name of the email list you want to retrieve.
   * @return An EmailListEntry object of the retrieved email list.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrieveGroup(String)}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListEntry retrieveEmailList(String emailList)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Retrieving email list '" + emailList + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "emailList/" + SERVICE_VERSION + "/" + emailList);
    return emailListService.getEntry(retrieveUrl, EmailListEntry.class);
  }

  /**
   * Deletes an email list.
   *
   * @param emailList The email list you with to delete.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#deleteGroup(String)}
   *             with Groups instead.
   */
  @Deprecated
  public void deleteEmailList(String emailList)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Attempting to delete emailList '" + emailList + "'.");

    URL deleteUrl = new URL(domainUrlBase + "emailList/" + SERVICE_VERSION + "/" + emailList);
    emailListService.delete(deleteUrl);
  }

  /**
   * Retrieves all recipients in an email list. This method may be very slow for
   * email lists with a large number of recipients. Any changes to the email
   * list contents, including adding or deleting recipients which are made after
   * this method is called may or may not be included in the Feed which is
   * returned.
   * 
   * @return An EmailListRecipientFeed object of the retrieved recipients.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrieveAllMembers(String)}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListRecipientFeed retrieveAllRecipients(String emailList)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO,
        "Retrieving all recipients in emailList '" + emailList + "'.");

    URL retrieveUrl = new URL(domainUrlBase + "emailList/"
        + SERVICE_VERSION + "/" + emailList + "/recipient/");

    EmailListRecipientFeed allRecipients = new EmailListRecipientFeed();
    EmailListRecipientFeed currentPage;
    Link nextLink;

    do {
      currentPage = emailListRecipientService.getFeed(retrieveUrl, EmailListRecipientFeed.class);
      allRecipients.getEntries().addAll(currentPage.getEntries());
      nextLink = currentPage.getLink(Link.Rel.NEXT, Link.Type.ATOM);
      if (nextLink != null) {
        retrieveUrl = new URL(nextLink.getHref());
      }
    } while (nextLink != null);

    return allRecipients;
  }

  /**
   * Retrieves one page (100) of recipients in an email list. Changes to the
   * email list recipients including creations and deletions, which are made
   * after this method is called may or may not be included in the Feed which is
   * returned. If the optional startRecipient parameter is specified, one page
   * of recipients is returned which have email addresses at or after
   * startRecipient as per ASCII value ordering with case-insensitivity. A value
   * of null or empty string indicates you want results from the beginning of
   * the list.
   *
   * @param emailList The name of the email list for which we are retrieving
   *        recipients.
   * @param startRecipient The starting point of the page (optional).
   * @return A EmailListRecipientFeed object of the retrieved recipients.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#retrievePageOfMembers(Link)}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListRecipientFeed retrievePageOfRecipients(String emailList,
      String startRecipient) throws AppsForYourDomainException,
      ServiceException, IOException {

    LOGGER.log(Level.INFO, "Retrieving one page of recipients"
        + (startRecipient != null ? " starting at " + startRecipient : "") + ".");

    URL retrieveUrl =
        new URL(domainUrlBase + "emailList/" + SERVICE_VERSION + "/" + emailList + "/recipient/");
    AppsForYourDomainQuery query = new AppsForYourDomainQuery(retrieveUrl);
    query.setStartRecipient(startRecipient);
    return emailListRecipientService.query(query, EmailListRecipientFeed.class);
  }

  /**
   * Adds an email address to an email list.
   *
   * @param recipientAddress The email address you wish to add.
   * @param emailList The email list you wish to modify.
   * @return The EmailListRecipientEntry of the newly created email list
   *         recipient.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#addMemberToGroup(String,String)}
   *             with Groups instead.
   */
  @Deprecated
  public EmailListRecipientEntry addRecipientToEmailList(
      String recipientAddress, String emailList)
      throws AppsForYourDomainException, ServiceException, IOException {

    LOGGER.log(Level.INFO, "Adding '" + recipientAddress + "' to emailList '" + emailList + "'.");

    EmailListRecipientEntry emailListRecipientEntry = new EmailListRecipientEntry();
    Who who = new Who();
    who.setEmail(recipientAddress);
    emailListRecipientEntry.addExtension(who);

    URL insertUrl =
        new URL(domainUrlBase + "emailList/" + SERVICE_VERSION + "/" + emailList + "/recipient");
    return emailListRecipientService.insert(insertUrl, emailListRecipientEntry);
  }

  /**
   * Removes an email address from an email list.
   *
   * @param recipientAddress The email address you wish to remove.
   * @param emailList The email list you wish to modify.
   * @throws AppsForYourDomainException If a Provisioning API specific occurs.
   * @throws ServiceException If a generic GData framework error occurs.
   * @throws IOException If an error occurs communicating with the GData
   *         service.
   * @deprecated Email lists have been replaced by Groups. Use
   *             {@link AppsGroupsService#deleteMemberFromGroup(String,String)}
   *             with Groups instead.
   */
  @Deprecated
  public void removeRecipientFromEmailList(String recipientAddress,
      String emailList) throws AppsForYourDomainException, ServiceException,
      IOException {

    LOGGER.log(Level.INFO, "Removing '" + recipientAddress + "' from emailList '" + emailList
        + "'.");

    URL deleteUrl =
        new URL(domainUrlBase + "emailList/" + SERVICE_VERSION + "/" + emailList + "/recipient/"
            + recipientAddress);
    emailListRecipientService.delete(deleteUrl);
  }


  /**
   * Main entry point.  Parses arguments and creates and invokes the
   * AppsForYourDomainClient.
   *
   * Usage: java AppsForYourDomainClient --admin_email [email]
   *                                     --admin_password [pass]
   *                                     --domain [domain]
   */
  public static void main(String[] arg)
      throws Exception {
    SimpleCommandLineParser parser = new SimpleCommandLineParser(arg);
    String adminEmail = parser.getValue("admin_email", "email", "e");
    String adminPassword = parser.getValue("admin_password", "pass", "p");
    String domain = parser.getValue("domain", "domain", "d");

    boolean help = parser.containsKey("help", "h");
    if (help || (adminEmail == null) || (adminPassword == null) || (domain == null)) {
      usage();
      System.exit(1);
    }

    AppsForYourDomainClient client =
        new AppsForYourDomainClient(adminEmail, adminPassword, domain);
    client.run();
  }

  /*
   * Prints the command line usage of this sample application.
   */
  private static void usage() {
    System.out.println("Usage: java AppsForYourDomainClient" +
        " --admin_email [email] --admin_password [pass] --domain [domain]");
    System.out.println(
        "\nA simple application that performs user, email list,\n" +
        "and nickname related operations on the given domain using.\n" +
        "the provided admin username and password.\n");
  }
}
