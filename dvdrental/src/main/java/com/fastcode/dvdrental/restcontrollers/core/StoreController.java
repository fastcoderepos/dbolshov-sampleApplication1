package com.fastcode.dvdrental.restcontrollers.core;

import com.fastcode.dvdrental.application.core.address.IAddressAppService;
import com.fastcode.dvdrental.application.core.customer.ICustomerAppService;
import com.fastcode.dvdrental.application.core.customer.dto.FindCustomerByIdOutput;
import com.fastcode.dvdrental.application.core.inventory.IInventoryAppService;
import com.fastcode.dvdrental.application.core.inventory.dto.FindInventoryByIdOutput;
import com.fastcode.dvdrental.application.core.staff.IStaffAppService;
import com.fastcode.dvdrental.application.core.staff.dto.FindStaffByIdOutput;
import com.fastcode.dvdrental.application.core.store.IStoreAppService;
import com.fastcode.dvdrental.application.core.store.dto.*;
import com.fastcode.dvdrental.commons.logging.LoggingHelper;
import com.fastcode.dvdrental.commons.search.OffsetBasedPageRequest;
import com.fastcode.dvdrental.commons.search.SearchCriteria;
import com.fastcode.dvdrental.commons.search.SearchUtils;
import java.time.*;
import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    @Qualifier("storeAppService")
    @NonNull
    protected final IStoreAppService _storeAppService;

    @Qualifier("addressAppService")
    @NonNull
    protected final IAddressAppService _addressAppService;

    @Qualifier("customerAppService")
    @NonNull
    protected final ICustomerAppService _customerAppService;

    @Qualifier("inventoryAppService")
    @NonNull
    protected final IInventoryAppService _inventoryAppService;

    @Qualifier("staffAppService")
    @NonNull
    protected final IStaffAppService _staffAppService;

    @NonNull
    protected final LoggingHelper logHelper;

    @NonNull
    protected final Environment env;

    @PreAuthorize("hasAnyAuthority('STOREENTITY_CREATE')")
    @RequestMapping(method = RequestMethod.POST, consumes = { "application/json" }, produces = { "application/json" })
    public ResponseEntity<CreateStoreOutput> create(@RequestBody @Valid CreateStoreInput store) {
        CreateStoreOutput output = _storeAppService.create(store);
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("No record found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }

    // ------------ Delete store ------------
    @PreAuthorize("hasAnyAuthority('STOREENTITY_DELETE')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public void delete(@PathVariable String id) {
        FindStoreByIdOutput output = _storeAppService.findById(Integer.valueOf(id));
        Optional
            .ofNullable(output)
            .orElseThrow(
                () -> new EntityNotFoundException(String.format("There does not exist a store with a id=%s", id))
            );

        _storeAppService.delete(Integer.valueOf(id));
    }

    // ------------ Update store ------------
    @PreAuthorize("hasAnyAuthority('STOREENTITY_UPDATE')")
    @RequestMapping(
        value = "/{id}",
        method = RequestMethod.PUT,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity<UpdateStoreOutput> update(
        @PathVariable String id,
        @RequestBody @Valid UpdateStoreInput store
    ) {
        FindStoreByIdOutput currentStore = _storeAppService.findById(Integer.valueOf(id));
        Optional
            .ofNullable(currentStore)
            .orElseThrow(
                () -> new EntityNotFoundException(String.format("Unable to update. Store with id=%s not found.", id))
            );

        store.setVersiono(currentStore.getVersiono());
        UpdateStoreOutput output = _storeAppService.update(Integer.valueOf(id), store);
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("No record found")));
        return new ResponseEntity(output, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(
        value = "/{id}",
        method = RequestMethod.GET,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity<FindStoreByIdOutput> findById(@PathVariable String id) {
        FindStoreByIdOutput output = _storeAppService.findById(Integer.valueOf(id));
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("Not found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(method = RequestMethod.GET, consumes = { "application/json" }, produces = { "application/json" })
    public ResponseEntity find(
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "offset", required = false) String offset,
        @RequestParam(value = "limit", required = false) String limit,
        Sort sort
    ) throws Exception {
        if (offset == null) {
            offset = env.getProperty("fastCode.offset.default");
        }
        if (limit == null) {
            limit = env.getProperty("fastCode.limit.default");
        }

        if (sort == null || sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "storeId");
        }

        Pageable Pageable = new OffsetBasedPageRequest(Integer.parseInt(offset), Integer.parseInt(limit), sort);
        SearchCriteria searchCriteria = SearchUtils.generateSearchCriteriaObject(search);

        return ResponseEntity.ok(_storeAppService.find(searchCriteria, Pageable));
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(
        value = "/{id}/address",
        method = RequestMethod.GET,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity<GetAddressOutput> getAddress(@PathVariable String id) {
        GetAddressOutput output = _storeAppService.getAddress(Integer.valueOf(id));
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("Not found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(
        value = "/{id}/customers",
        method = RequestMethod.GET,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity getCustomers(
        @PathVariable String id,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "offset", required = false) String offset,
        @RequestParam(value = "limit", required = false) String limit,
        Sort sort
    ) throws Exception {
        if (offset == null) {
            offset = env.getProperty("fastCode.offset.default");
        }
        if (limit == null) {
            limit = env.getProperty("fastCode.limit.default");
        }

        if (sort == null || sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "customerId");
        }

        Pageable pageable = new OffsetBasedPageRequest(Integer.parseInt(offset), Integer.parseInt(limit), sort);

        SearchCriteria searchCriteria = SearchUtils.generateSearchCriteriaObject(search);
        Map<String, String> joinColDetails = _storeAppService.parseCustomersJoinColumn(id);
        Optional
            .ofNullable(joinColDetails)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Invalid join column")));

        searchCriteria.setJoinColumns(joinColDetails);

        List<FindCustomerByIdOutput> output = _customerAppService.find(searchCriteria, pageable);
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("Not found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(
        value = "/{id}/inventorys",
        method = RequestMethod.GET,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity getInventorys(
        @PathVariable String id,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "offset", required = false) String offset,
        @RequestParam(value = "limit", required = false) String limit,
        Sort sort
    ) throws Exception {
        if (offset == null) {
            offset = env.getProperty("fastCode.offset.default");
        }
        if (limit == null) {
            limit = env.getProperty("fastCode.limit.default");
        }

        if (sort == null || sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "inventoryId");
        }

        Pageable pageable = new OffsetBasedPageRequest(Integer.parseInt(offset), Integer.parseInt(limit), sort);

        SearchCriteria searchCriteria = SearchUtils.generateSearchCriteriaObject(search);
        Map<String, String> joinColDetails = _storeAppService.parseInventorysJoinColumn(id);
        Optional
            .ofNullable(joinColDetails)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Invalid join column")));

        searchCriteria.setJoinColumns(joinColDetails);

        List<FindInventoryByIdOutput> output = _inventoryAppService.find(searchCriteria, pageable);
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("Not found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('STOREENTITY_READ')")
    @RequestMapping(
        value = "/{id}/staffs",
        method = RequestMethod.GET,
        consumes = { "application/json" },
        produces = { "application/json" }
    )
    public ResponseEntity getStaffs(
        @PathVariable String id,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "offset", required = false) String offset,
        @RequestParam(value = "limit", required = false) String limit,
        Sort sort
    ) throws Exception {
        if (offset == null) {
            offset = env.getProperty("fastCode.offset.default");
        }
        if (limit == null) {
            limit = env.getProperty("fastCode.limit.default");
        }

        if (sort == null || sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "staffId");
        }

        Pageable pageable = new OffsetBasedPageRequest(Integer.parseInt(offset), Integer.parseInt(limit), sort);

        SearchCriteria searchCriteria = SearchUtils.generateSearchCriteriaObject(search);
        Map<String, String> joinColDetails = _storeAppService.parseStaffsJoinColumn(id);
        Optional
            .ofNullable(joinColDetails)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Invalid join column")));

        searchCriteria.setJoinColumns(joinColDetails);

        List<FindStaffByIdOutput> output = _staffAppService.find(searchCriteria, pageable);
        Optional.ofNullable(output).orElseThrow(() -> new EntityNotFoundException(String.format("Not found")));

        return new ResponseEntity(output, HttpStatus.OK);
    }
}
