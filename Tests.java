package BACKOFFICE_FRONT_END_TESTS;

import BACKEND_TESTS.dataModels.PosData;
import BACKOFFICE_FRONT_END_TESTS.methods.*;
import BACKOFFICE_FRONT_END_TESTS.setup.BackofficeCredentials;
import FRAMEWORK_BASE.TestLogger;
import FRAMEWORK_BASE.TestNgRunner;
import FRAMEWORK_BASE.Utils;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static BACKEND_TESTS.TestData.*;
import static BACKEND_TESTS.enums.TransactionStatuses.TRANSACTION_NOT_ALLOWED;
import static BACKEND_TESTS.enums.TransactionStatuses.TRANSFER_PASSED;
import static BACKEND_TESTS.main.MainTestSteps.*;
import static BACKEND_TESTS.services.enrollment.EnrollmentUtil.getPosToken;
import static BACKEND_TESTS.services.transaction.TransactionUtil.initTransaction;

public class Tests extends TestNgRunner {

    @BeforeMethod()
    public void prepareTest() {
    }

    @Test(groups = {"backoffice"}, description = "Add Merchant/Contact/Branch/Online Shop/ITCWallet/POS/Unassigned POS - Delete POS")
    public void fullFlow() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);
        PosMethods pm = new PosMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add new Contact
        mm.addMerchantContact();
        mm.fillAllDataForNewContact();
        mm.contactSaved();
        // When - User add new branch
        mm.addMerchantBranch();
        mm.fillAllDataForNewBranch();
        mm.fillAllDataForNewContact();
        mm.branchSaved("Testowy");
        // When - User add online shop
        mm.addOnlineShop();
        mm.fillAllDataOnlineShop();
        mm.onlineShopSaved();
        // When - User add ITC Wallet
        mm.addItcWallet();
        String midNumber = mm.fillAllDataItcWallet();
        mm.itcWalletSaved(midNumber);
        // When -  Login on it support
        login.logInMethod(driver, BackofficeCredentials.IT_SUPPORT);
        // When - User add POS
        mpm.findMerchantOnList(vatNumber);
        mm.addPos();
        PosData.Pos testPos = mm.fillAllDataOnPos();
        mm.posSaved(testPos.serialNo);
        // Then - User delete POS
        mm.deletePos(testPos.serialNo);
        // Then - User add unassigned POS
        mpm.goToPosTab();
        pm.addUnassignedPos(testPos.serialNo, vatNumber);
        pm.saveForm();
        //tu nie powinno być pm zamiast mm?
    }

    @Test(groups = {"backoffice"}, description = "Modify/Delete ITC Wallet")
    public void itcWalletModifyAndDelete() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add new branch
        mm.addMerchantBranch();
        mm.fillAllDataForNewBranch();
        mm.fillAllDataForNewContact();
        mm.branchSaved("Testowy");
        // When - User add ITC Wallet
        mm.addItcWallet();
        String midNumber = mm.fillAllDataItcWallet();
        mm.itcWalletSaved(midNumber);
        // When - User modify ITC Wallet
        String midNumberMod = mm.modifyItcWallet();
        // When -  Login on it support
        login.logInMethod(driver, BackofficeCredentials.IT_SUPPORT);
        // When - User add POS
        mpm.findMerchantOnList(vatNumber);
        mm.addPos();
        PosData.Pos testPos = mm.fillAllDataOnPos();
        mm.posSaved(testPos.serialNo);
        // When - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        mpm.goToMerchantTab();
        mpm.findMerchantOnList(vatNumber);
        // When - User delete ITC Wallet
        // Then - Notification appear
        mm.deleteItcWallet();
        mm.notificationVisibleVisibility(midNumberMod);
        // When -  Login on it support
        login.logInMethod(driver, BackofficeCredentials.IT_SUPPORT);
        mpm.goToMerchantTab();
        mpm.findMerchantOnList(vatNumber);
        // Then - User delete POS
        mm.deletePos(testPos.serialNo);
        // When - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        mpm.goToMerchantTab();
        mpm.findMerchantOnList(vatNumber);
        // When - User delete ITC Wallet
        // Then - ITC wallet deleted
        mm.deleteItcWallet();
        mm.walletOnTheListInvisibility();
    }

    @Test(groups = {"backoffice"}, description = "Delete User Card, change status Active>Blocked>Deleted")
    public void deleteUserCard() {

        // Given - User add client with card
        long userId = registerNewUser(defaultUser.user);
        TestLogger.logger.info("User: " + userId + " was created");
        addValidCardForExistingUser(userId);
        String userPhone = rememberedPhoneNumber.get();

        driver = Utils.WebDriver.getNewDriver();
        LoginPageMethods login = new LoginPageMethods(driver);
        MainPageMethods mpm = new MainPageMethods(driver);
        ClientsMethods cm = new ClientsMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToClientsTab();
        cm.findClientOnList(userPhone, defaultUser.firstName);
        // When - User delete card
        // Then - card is deleted
        cm.deleteCard();
        // When - User change status - Blocked
        cm.changeClientStatus("2", "6", "Zablokowany");
        // When - User change status - Deleted
        cm.changeClientStatus("2", "13", "Usunięty");
        cm.clientStatusValidation();
    }

    @Test(groups = {"backoffice"}, description = "Add/Modify/Delete - Representatives")
    public void addRepresentatives() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add Representative
        mm.addRepresentatives();
        // THen - User modify Representative
        mm.representativesChangeData();
        // Then - User delete Representative
        mm.deleteRepresentatives();
    }

    @Test(groups = {"backoffice"}, description = "Change branch category")
    public void branchCategoryChange() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);


        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add new branch
        mm.addMerchantBranch();
        mm.fillAllDataForNewBranch();
        mm.fillAllDataForNewContact();
        mm.branchSaved("Testowy");
        // Then - category change
        mm.branchCategoryChange();
    }

    @Test(groups = {"backoffice"}, description = "Add/modify POS agreement")
    public void agreementsFlow() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);


        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add new branch
        mm.addMerchantBranch();
        mm.fillAllDataForNewBranch();
        mm.fillAllDataForNewContact();
        mm.branchSaved("Testowy");
        // When - User add ITC Wallet
        mm.addItcWallet();
        String midNumber = mm.fillAllDataItcWallet();
        mm.itcWalletSaved(midNumber);
        // When -  Login on it support
        login.logInMethod(driver, BackofficeCredentials.IT_SUPPORT);
        // When - User add POS
        mpm.findMerchantOnList(vatNumber);
        mm.addPos();
        PosData.Pos testPos = mm.fillAllDataOnPos();
        mm.posSaved(testPos.serialNo);
        // When - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - Find Merchant
        mpm.findMerchantOnList(vatNumber);
        // When - User change agreement on POS
        mm.posAgreementAdd();
        mm.posAgreementChangeStatus("Nasza werifikacja", "VERIFICATION_NEGATIVE_OUR", "Odrzucony", "Nasza weryfikacja negatywna");
        mm.posAgreementChangeStatus("Weryfikacja zewnęntrzna", "VERIFICATION_NEGATIVE_FOREIGN", "Odrzucony", "Zewnętrzna weryfikacja negatywna");
        mm.posAgreementChangeStatusActive("Aktywny", "AGREEMENT_TERMINATION", "Aktywny", "Wypowiedzenie umowy");
        mm.posAgreementChangeStatusBlocked("Aktywny", "BLOCKED", "Aktywny", "Zablokowany");
        // When - User change agreement on E-commerce
        mm.eCommerceAgreementAdd();
    }

    @Test(groups = {"backoffice"}, description = "Verify transactions filters")
    public void transactionsFlow() {
        // Given - User add client with card and transaction
        long userId = registerNewUser(defaultUser.user);
        PosData.Pos testPos = getPosForTest();
        System.out.println(userId);
        addValidCardForExistingUser(userId);
        Assert.assertTrue(waitForCreateTemplateForUser(userId, 60));
        long id = createValidTransactionForUser(testPos, userId, pin, defaultUser.faceVectorIdentification, 44.44, TRANSFER_PASSED, true).transaction.paymentId;
        String paymentId = String.valueOf(id);

        driver = Utils.WebDriver.getNewDriver();
        LoginPageMethods login = new LoginPageMethods(driver);
        MainPageMethods mpm = new MainPageMethods(driver);
        TransactionsMethods tm = new TransactionsMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToTransactionTab();
        // When - User fond transactions
        tm.findTransactionOnList("44.44");
        //Then - User verify transaction details
        tm.transactionDetailsVerificationPOS();
        mpm.goToTransactionTab();
        // Then - User verify transaction filters
        tm.transactionFilterVerifyVisibility("amountFrom", "44.44", "44.44");
        tm.transactionFilterVerifyVisibility("amountTo", "44.44", "44.44");
        tm.transactionFilterVerifyVisibility("clientName", "MaciejSz", "MaciejSz");
        tm.transactionFilterVerifyVisibility("clientLastName", "Automated", "Automated");
        tm.transactionFilterVerifyVisibility("branchName", "san escobar", "san escobar");
        tm.transactionFilterVerifyVisibility("fromUserIds", String.valueOf(userId), String.valueOf(userId));
        //tm.transactionFilterVerifyVisibility("correlationIds", paymentId, paymentId);
        tm.transactionFilterVerifyInvisibility("clientName", "MaciejSzTest", "");
        tm.transactionFilterVerifyInvisibility("clientLastName", "AutomatedTest", "");
        tm.transactionFilterVerifyInvisibility("branchName", "san escobar Test", "");
        tm.transactionFilterVerifyInvisibility("fromUserIds", "99999", "");
    }

    @Test(groups = {"backoffice"}, description = "Perform transaction for blocked POS")
    public void blockPosAndTransactionPerform() {
        // Given - User add client with card
        long userId = registerNewUser(defaultUser.user);
        System.out.println(userId);
        addValidCardForExistingUser(userId);

        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - User add new branch
        mm.addMerchantBranch();
        mm.fillAllDataForNewBranch();
        mm.fillAllDataForNewContact();
        mm.branchSaved("Testowy");
        // When - User add ITC Wallet
        mm.addItcWallet();
        String midNumber = mm.fillAllDataItcWallet();
        mm.itcWalletSaved(midNumber);
        // When -  Login on it support
        login.logInMethod(driver, BackofficeCredentials.IT_SUPPORT);
        // When - User add POS
        mpm.findMerchantOnList(vatNumber);
        mm.addPos();
        PosData.Pos testPos = mm.fillAllDataOnPos();
        mm.posSaved(testPos.serialNo);
        // When - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - Find Merchant
        mpm.findMerchantOnList(vatNumber);
        // When - User change agreement on POS - Blocked
        mm.posAgreementAdd();
        mm.posAgreementChangeStatusBlocked("Aktywny", "BLOCKED", "Aktywny", "Zablokowany");
        // The - User preform transaction
        String posToken = getPosToken(testPos);
        Response initTransactionResponse = initTransaction(posToken, 12.12, "PLN");
        Assert.assertEquals(RestAPIUtils.getValueFromResponse(initTransactionResponse, "status").asString(), TRANSACTION_NOT_ALLOWED.getStatus());
    }

    @Test(groups = {"backoffice"}, description = "Changes history on merchant")
    public void changesHistory() {
        driver = Utils.WebDriver.getNewDriver();
        MainPageMethods mpm = new MainPageMethods(driver);
        MerchantsMethods mm = new MerchantsMethods(driver);
        LoginPageMethods login = new LoginPageMethods(driver);

        // Given - Login on op_senior
        login.logInMethod(driver, BackofficeCredentials.SENIOR_OPERATOR);
        // Given - User is on main page
        mpm.goToMerchantTab();
        // When - User add new Merchant
        String vatNumber = mpm.fillAllDateForNewMerchant();
        mm.saveForm();
        mpm.findMerchantOnList(vatNumber);
        // When - change merchant data
        mm.changeMerchantData();
        // Then - check changes history
        mm.checkChangesHistory();
    }
}


