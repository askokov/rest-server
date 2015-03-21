package ru.uss.esf.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

import ru.uss.esf.api.exception.BusinessException;
import ru.uss.esf.api.invoice.InvoiceByIdWithReasonRequest;
import ru.uss.esf.api.invoice.InvoiceIdWithReason;
import ru.uss.esf.api.invoice.QueryInvoiceResponse;
import ru.uss.esf.api.upload.Error;
import ru.uss.esf.api.upload.StandardResponse;
import ru.uss.esf.api.upload.SyncInvoiceResponse;
import ru.uss.esf.client.Client;
import ru.uss.esf.controller.exception.NotFoundException;
import ru.uss.esf.facade.InvoiceInfoManager;
import ru.uss.esf.model.ChangeStatusResult;
import ru.uss.esf.model.InvoiceCategory;
import ru.uss.esf.model.invoice.InvoiceDirection;
import ru.uss.esf.model.invoice.InvoiceInfo;
import ru.uss.esf.model.invoice.InvoiceInfoHistoryRecord;
import ru.uss.esf.model.invoice.InvoiceSummary;
import ru.uss.esf.model.invoice.abstractinvoice.AbstractInvoice;
import ru.uss.esf.model.invoice.abstractinvoice.InvoiceStatus;
import ru.uss.esf.model.invoice.abstractinvoice.InvoiceType;
import ru.uss.esf.model.invoice.container.InvoiceContainer;
import ru.uss.esf.model.invoice.container.InvoiceInfoContainer;
import ru.uss.esf.model.usermng.RoleConstants;
import ru.uss.esf.restdto.HashOrErrors;
import ru.uss.esf.restdto.RelatedId;
import ru.uss.esf.restdto.upload.InvoiceImportResponse;
import ru.uss.esf.restdto.upload.UploadedFile;
import ru.uss.esf.security.CurrentUserLocator;
import ru.uss.esf.service.invoice.DraftServiceLocal;
import ru.uss.esf.service.invoice.InvoiceServiceLocal;
import ru.uss.esf.service.invoice.UploadInvoiceServiceLocal;
import ru.uss.esf.util.DateTimeUtils;
import ru.uss.esf.util.JsonHelper;
import ru.uss.esf.util.RequestUtil;

@RestController
@RequestMapping(value = "/invoice")
class InvoiceController {
    @Autowired
    private InvoiceServiceLocal invoiceServiceLocal;

    @Autowired
    private DraftServiceLocal draftServiceLocal;

    @Autowired
    private CurrentUserLocator currentUserLocator;

    @Autowired
    private UploadInvoiceServiceLocal uploadInvoiceService;

    @Autowired
    private InvoiceInfoManager invoiceInfoManager;

    private JAXBContext exportContext;

    @PostConstruct
    void initExportContext() throws Exception {
        exportContext = JAXBContext.newInstance(InvoiceInfoContainer.class);
    }

    //Search methods
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    InvoiceInfo getById(@PathVariable Long id) {
        InvoiceInfo invoiceInfo = invoiceServiceLocal.getById(id);

        if (invoiceInfo == null) {
            throw new ru.uss.esf.controller.exception.NotFoundException();
        }

        return invoiceInfo;
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/number", method = RequestMethod.GET)
    InvoiceInfo[] getByNum(@RequestParam String num, @RequestParam String date) throws ParseException {
        String tin = currentUserLocator.getCurrentUserClient().getTin();

        InvoiceInfo invoiceInfo = invoiceServiceLocal.getByDateAndNum(tin, new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(date), num);

        if (invoiceInfo == null) {
            throw new ru.uss.esf.controller.exception.NotFoundException();
        }

        return new InvoiceInfo[]{invoiceInfo};
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/regNumber/{regNum}", method = RequestMethod.GET)
    QueryInvoiceResponse getByRegNum(@PathVariable String regNum) {
        InvoiceInfo invoiceInfo = invoiceServiceLocal.getByRegNumber(regNum);

        if (invoiceInfo == null) {
            return new QueryInvoiceResponse(Collections.<InvoiceInfo>emptyList(), true, 1);
        }

        return new QueryInvoiceResponse(Collections.singletonList(invoiceInfo), true, 1);
    }

    /**
     * Находим родительскую СФ, а также СФ-ки которые ссылаются на переданную
     */
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/invoiceTree", method = RequestMethod.GET)
    List<InvoiceInfo> getInvoiceTree(@RequestParam String regNum) {
        List<InvoiceInfo> list = new ArrayList<>();

        InvoiceInfo currentInvoiceInfo = invoiceServiceLocal.getByRegNumber(regNum);

        if (currentInvoiceInfo.getInvoice().getRelatedInvoice() != null) {
            list.add(invoiceServiceLocal.getByDateAndNum(currentInvoiceInfo.getInvoice().getCreatorTin(), currentInvoiceInfo.getInvoice().getRelatedInvoice().getDate(), currentInvoiceInfo.getInvoice().getRelatedInvoice().getNum()));
        }

        for (InvoiceInfo relatedInvoice : invoiceServiceLocal.getActiveRelatedInvoices(currentInvoiceInfo.getInvoice().getCreatorTin(), currentInvoiceInfo.getInvoice().getDate(), currentInvoiceInfo.getInvoice().getNum())) {
            list.add(relatedInvoice);
        }

        return list;
    }
    
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/activeRelatedInvoices", method = RequestMethod.GET)
    public @ResponseBody List<RelatedId> getActiveRelatedInvoices(@RequestParam String num, @RequestParam String date) throws ParseException {
        List<RelatedId> list = new ArrayList<>();

        String tin = currentUserLocator.getCurrentUserClient().getTin();

        InvoiceInfo invoiceInfo = invoiceServiceLocal.getByDateAndNum(tin, new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(date), num);

        RelatedId mainInvoiceId = null;

        //основная СФ может отсутствовать, если дата её выписки раньше даты старта системы
        if (invoiceInfo != null && Arrays.asList(InvoiceStatus.CREATED, InvoiceStatus.DELIVERED).contains(invoiceInfo.getInvoiceStatus())) {
            mainInvoiceId = new RelatedId(invoiceInfo.getInvoiceId(), invoiceInfo.getInvoice().getNum(), invoiceInfo.getInvoice().getDate());
        }

        list.add(mainInvoiceId);

        for (InvoiceInfo additionalInvoice : invoiceServiceLocal.getActiveRelatedInvoices(tin, new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(date), num)) {
            list.add(new RelatedId(additionalInvoice.getInvoiceId(), additionalInvoice.getInvoice().getNum(), additionalInvoice.getInvoice().getDate()));
        }

        return list;
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/searchlist", method = RequestMethod.GET)
    QueryInvoiceResponse searchList(@RequestParam("tins[]") List<String> tins,
                                    @RequestParam(required = false) InvoiceDirection direction,
                                    @RequestParam(required = false) String contragentTin,
                                    @RequestParam(required = false) String dateFrom,
                                    @RequestParam(required = false) String dateTo,
                                    @RequestParam("statuses[]") List<InvoiceStatus> statuses,
                                    @RequestParam(required = false) InvoiceType type,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false, defaultValue = RequestUtil.DEFAULT_SORT_ORDER) String order,
                                    @RequestParam(required = false, defaultValue = "1") int pageNum,
                                    @RequestParam(required = false, defaultValue = "1000") int rows) throws ParseException {
        Date from = dateFrom == null ? null : DateTimeUtils.startOfTheDay(new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(dateFrom));
        Date to = dateTo == null ? null : DateTimeUtils.endOfTheDay(new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(dateTo));
        
        QueryInvoiceResponse response = invoiceServiceLocal.findInvoices(tins, direction, contragentTin, from, to, statuses, type, sort, RequestUtil.isAsc(order), pageNum, rows);

        return response;
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/counts", method = RequestMethod.GET)
    Map<String, Integer> counts(@RequestParam("tins[]") List<String> tins, @RequestParam(required = false) String contragentTin) {
        Map<String, Integer> result = new HashMap<>();

        for (InvoiceCategory c : Arrays.asList(InvoiceCategory.values())) {
            result.put(c.name(), invoiceServiceLocal.findInvoicesCount(tins, c.getDirection(), contragentTin, null, null, c.getStatuses(), c.getType()));
        }

        result.put("INWORK_DRAFT", draftServiceLocal.countDrafts());
        result.put("INWORK_TOTAL", result.get("INWORK_DRAFT") + result.get(InvoiceCategory.INWORK_IMPORTED.name()) + result.get(InvoiceCategory.INWORK_FAILED.name()));

        return result;
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/idWithReasonListHash", method = RequestMethod.POST)
    Map<String, String> createHashForIdWithReasonList(@RequestParam String list) {
        List<InvoiceIdWithReason> invoiceIds = JsonHelper.readValueAsList(list, InvoiceIdWithReason.class);

        String hash = invoiceServiceLocal.createHashForIdWithReasonList(invoiceIds);

        return Collections.singletonMap("hash", hash);
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/hash", method = RequestMethod.POST)
    Map<String, String> createHashInvoice(@RequestParam String invoice) {
        AbstractInvoice invoiceObject = JsonHelper.readValue(invoice, AbstractInvoice.class);

        String hash = invoiceServiceLocal.getHash(invoiceObject);

        return Collections.singletonMap("hash", hash);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    Map<String, List<Error>> validate(@RequestParam String invoice) {
        AbstractInvoice invoiceObject = JsonHelper.readValue(invoice, AbstractInvoice.class);

        InvoiceInfo info = new InvoiceInfo();

        info.setInvoice(invoiceObject);

        List<Error> errors = invoiceServiceLocal.getHashesOrErrors(Arrays.asList(info), invoiceObject.getDate()).values().iterator().next().getErrors();

        return Collections.singletonMap("errors", errors);
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_CREATE_REGULAR)
    @RequestMapping(value = "/copy/{id}", method = RequestMethod.GET)
    InvoiceInfo copyInvoice(@PathVariable long id) {
        AbstractInvoice newInvoice = invoiceServiceLocal.copy(id);

        if (newInvoice == null) throw new NotFoundException();
        
        InvoiceInfo info = new InvoiceInfo();
        
        info.setInvoice(newInvoice);

        return info;
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_REVOKE)
    @RequestMapping(value = "/revoke", method = RequestMethod.PUT)
    Map<String, List<ChangeStatusResult>> revoke(@RequestParam String list,
                                                 @RequestParam String certificate,
                                                 @RequestParam String signature) throws BusinessException {
        try {
            List<InvoiceIdWithReason> ids = JsonHelper.readValueAsList(list, InvoiceIdWithReason.class);

            InvoiceByIdWithReasonRequest request = new InvoiceByIdWithReasonRequest();

            request.setIdWithReasonList(ids);
            request.setSignature(signature);
            request.setX509Certificate(certificate);

            return Collections.singletonMap("statusList", getClient().revokeSigned(request));
        } catch (ru.uss.esf.api.exception.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage()); // map exception type in order to set right access denied status 403
        }
    }
    
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/unrevoke", method = RequestMethod.PUT)
    Map<String, List<ChangeStatusResult>> unrevoke(@RequestParam String list,
                                                   @RequestParam String certificate,
                                                   @RequestParam String signature) throws BusinessException {
        try {
            List<InvoiceIdWithReason> ids = JsonHelper.readValueAsList(list, InvoiceIdWithReason.class);

            InvoiceByIdWithReasonRequest request = new InvoiceByIdWithReasonRequest();

            request.setIdWithReasonList(ids);
            request.setSignature(signature);
            request.setX509Certificate(certificate);

            return Collections.singletonMap("statusList", getClient().unrevokeSigned(request));
        } catch (ru.uss.esf.api.exception.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage()); // map exception type in order to set right access denied status 403
        }
    }
    
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/decline", method = RequestMethod.PUT)
    Map<String, List<ChangeStatusResult>> decline(@RequestParam String list,
                                                  @RequestParam String certificate,
                                                  @RequestParam String signature) throws BusinessException {
        try {
            List<InvoiceIdWithReason> ids = JsonHelper.readValueAsList(list, InvoiceIdWithReason.class);

            InvoiceByIdWithReasonRequest request = new InvoiceByIdWithReasonRequest();

            request.setIdWithReasonList(ids);
            request.setSignature(signature);
            request.setX509Certificate(certificate);

            return Collections.singletonMap("statusList", getClient().declineSigned(request));
        } catch (ru.uss.esf.api.exception.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage()); // map exception type in order to set right access denied status 403
        }
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/confirm", method = RequestMethod.PUT)
    Map<String, List<InvoiceSummary>> confirm(@RequestParam String list) throws BusinessException {
        try {
            List<Long> invoiceIds = JsonHelper.readValueAsList(list, Long.class);

            return Collections.singletonMap("statusList", getClient().confirm(invoiceIds));
        } catch (ru.uss.esf.api.exception.AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage()); // map exception type in order to set right access denied status 403
        }
    }

    //Если используем в качестве источника СФ черновик, то необходимо удалить его после создания СФ на клиенте
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    StandardResponse createInvoice(@RequestParam String invoice,
                                   @RequestParam String certificate) throws BusinessException {
        AbstractInvoice invoiceObj = JsonHelper.readValue(invoice, AbstractInvoice.class);

        SyncInvoiceResponse response = invoiceServiceLocal.createInvoice(invoiceObj, certificate, getClient(), invoiceObj.getDate());
        
        return response.getDeclinedSet().isEmpty() ? response.getAcceptedSet().get(0) : response.getDeclinedSet().get(0);  
    }

    //Imported--------------------------------------------------------------------------------------------
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)//всё равно при удалении проверяется создатель, поэтому удалить чужое нельзя
    @RequestMapping(value = "/deleteImported", method = RequestMethod.DELETE)
    void deleteImported(@RequestParam String list) {
        List<Long> invoiceIds = JsonHelper.readValueAsList(list, Long.class);

        invoiceServiceLocal.deleteWithStatus(invoiceIds, InvoiceStatus.IMPORTED);
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/hashImported", method = RequestMethod.POST)
    Map<Long, HashOrErrors> hashImported(@RequestParam String list, @RequestParam String localTime) throws ParseException {
        List<Long> invoiceIds = JsonHelper.readValueAsList(list, Long.class);

        Date clientDate = new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(localTime);

        return invoiceServiceLocal.getHashesOrErrors(invoiceInfoManager.load(invoiceIds), clientDate);
    }

    @RequestMapping(value = "/sendSignedImported", method = RequestMethod.POST)
    SyncInvoiceResponse sendSignedImported(@RequestParam String signatures,
                                           @RequestParam String certificate,
                                           @RequestParam String localTime) throws BusinessException, ParseException {
        Map<Long, String> map = JsonHelper.readValueAsMap(signatures, Long.class, String.class);

        Date clientDate = new SimpleDateFormat(DateTimeUtils.DATE_PATTERN).parse(localTime);

        return invoiceServiceLocal.createFromImported(map, certificate, getClient(), clientDate);
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_CREATE_REGULAR)
    @RequestMapping(value = "/importFromFile", method = RequestMethod.POST)
    InvoiceImportResponse importFromFile(@RequestParam("filename") String fileName, HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String filePath = (String) session.getAttribute("FILEPATH_" + fileName);
            
            InvoiceContainer container = uploadInvoiceService.unmarshal(filePath);

            return uploadInvoiceService.validateAndSaveInvoices(container);
        } catch (Exception e) {
            InvoiceImportResponse resp = new InvoiceImportResponse();
            
            resp.setFatalError(new RequestContext(request).getMessage("message.error") + ": " + e.toString());
            
            return resp;
        }
    }

    @RolesAllowed(RoleConstants.ROLE_INVOICE_CREATE_REGULAR)
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    UploadedFile upload(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
        File tmpFile = uploadInvoiceService.saveTemporaryFile(file);

        session.setAttribute("FILEPATH_" + file.getOriginalFilename(), tmpFile.getPath());

        return new UploadedFile(file.getOriginalFilename(), Long.valueOf(file.getSize()).intValue());
    }

    //Failed-------------------------------------------------------------------------------------------
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)//всё равно при удалении проверяется создатель, поэтому удалить чужое нельзя
    @RequestMapping(value = "/deleteFailed", method = RequestMethod.DELETE)
    void deleteFailed(@RequestParam String list) {
        List<Long> invoiceIds = JsonHelper.readValueAsList(list, Long.class);

        invoiceServiceLocal.deleteWithStatus(invoiceIds, InvoiceStatus.FAILED);
    }

    //History-------------------------------------------------------------------------------------------
    @RolesAllowed({RoleConstants.ROLE_INVOICE_VIEW, RoleConstants.ROLE_INVOICE_SEARCH})
    @RequestMapping(value = "/history/{id}", method = RequestMethod.GET)
    List<InvoiceInfoHistoryRecord> getHistoryByInvoiceId(@PathVariable Long id) {
        return invoiceServiceLocal.getHistoryByInvoiceId(id);
    }
    
    //Queue-------------------------------------------------------------------------------------------
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    QueryInvoiceResponse getInQueue(@RequestParam(required = false, defaultValue = RequestUtil.DEFAULT_SORT_ORDER) String order,
                                    @RequestParam(required = false, defaultValue = "1") int pageNum,
                                    @RequestParam(required = false, defaultValue = "1000") int rows) {
        return invoiceServiceLocal.findInQueue(order, pageNum, rows);        
    }

    //Export to xml------------------------------------------------------------------------------------
    @RolesAllowed(RoleConstants.ROLE_INVOICE_VIEW)
    @RequestMapping(value = "/downloadXml", method = RequestMethod.GET)
    void downloadXml(@RequestParam("ids[]") List<Long> ids, @RequestParam(value = "draft", required = false, defaultValue = "false") Boolean isDraft, HttpServletResponse response) throws JAXBException, IOException {
        InvoiceInfoContainer container = new InvoiceInfoContainer();
        
        if (ids.size() > 1000) throw new IllegalStateException("Кол-во запрашиваемых ЭСФ не должно превышать 1000");

        if (isDraft) {
            container.setInvoiceSet(draftServiceLocal.getDrafts(ids));
        } else {
            container.setInvoiceSet(invoiceServiceLocal.getInvoices(ids));
        }

        response.setContentType(MediaType.APPLICATION_XML.toString());
        response.setHeader("Content-Disposition", "attachment; filename=export_esf.xml");

        Marshaller m = exportContext.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(container, response.getOutputStream());
    }

    //Print
    @RequestMapping(value = "/print", method = RequestMethod.GET)
    ModelAndView printInvoice(@RequestParam("id") Long id,
                              @RequestParam("isDraft") String isDraft) {
        ModelAndView modelAndView = new ModelAndView("print_form");

        modelAndView.addObject("isDraft", isDraft);
        modelAndView.addObject("invoice", JsonHelper.writeValueAsString(invoiceServiceLocal.getById(id)));

        return modelAndView;
    }
    
    @RolesAllowed(RoleConstants.ROLE_VIEW_LOG)
    @RequestMapping(value = "/printForAdmin/{id}", method = RequestMethod.GET)
    ModelAndView printForAdminInvoice(@PathVariable Long id) {                                      
        ModelAndView modelAndView = new ModelAndView("print_form");

        modelAndView.addObject("isDraft", false);
        modelAndView.addObject("invoice", JsonHelper.writeValueAsString(invoiceInfoManager.load(id)));

        return modelAndView;
    }
    
    /*
     * Для внутреннего использования
     */
    @RequestMapping(value = "/printPdf/{regnum}", method = RequestMethod.GET)
    ModelAndView printInvoiceX(@PathVariable String regnum, @RequestParam String lang, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("print_form");

        if (request.getRemoteAddr().equals("127.0.0.1") || request.getRemoteAddr().equals("0:0:0:0:0:0:0:1") || request.getRemoteAddr().equals("localhost")) {
            modelAndView.addObject("isDraft", false);        
            modelAndView.addObject("invoice", JsonHelper.writeValueAsString(invoiceInfoManager.getInvoiceByRegistrationNumber(regnum)));
            modelAndView.addObject("language", lang);
        }

        return modelAndView;
    }

    private Client getClient() {
        return currentUserLocator.getCurrentUserClient();
    }
}
