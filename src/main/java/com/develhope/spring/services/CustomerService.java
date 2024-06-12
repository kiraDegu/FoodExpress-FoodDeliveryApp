package com.develhope.spring.services;

import com.develhope.spring.daos.UserDetailsDao;
import com.develhope.spring.exceptions.InvalidCustomerException;
import com.develhope.spring.mappers.CustomerMapper;
import com.develhope.spring.models.ResponseCode;
import com.develhope.spring.models.ResponseModel;
import com.develhope.spring.models.dtos.CustomerDto;
import com.develhope.spring.models.entities.CustomerEntity;
import com.develhope.spring.daos.CustomerDao;
import com.develhope.spring.models.entities.UserDetailsEntity;
import com.develhope.spring.validators.CustomerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {


    private final CustomerDao customerDao;
    private final CustomerMapper customerMapper;
    private final CustomerValidator customerValidator;
    private final UserDetailsDao userDetailsDao;

    public CustomerService(CustomerDao customerDao, CustomerMapper customerMapper, CustomerValidator customerValidator, UserDetailsDao userDetailsDao) {
        this.customerDao = customerDao;
        this.customerMapper = customerMapper;
        this.customerValidator = customerValidator;
        this.userDetailsDao = userDetailsDao;
    }

    @Autowired


    /**
     * @param customerDto CustomerDto
     * @return a new Customer
     */
    public ResponseModel addCustomer(CustomerDto customerDto) {

        try {
            customerValidator.validateCustomer(customerDto);
            CustomerEntity newCustomer = customerMapper.toEntity(customerDto);
            CustomerEntity newCustomerEntity = customerDao.saveAndFlush(newCustomer);
            return new ResponseModel(ResponseCode.B, customerMapper.toDTO(newCustomerEntity));
        } catch (InvalidCustomerException e) {
            return new ResponseModel(ResponseCode.A).addMessageDetails(e.getMessage());
        }

    }

    /**
     * @param id customer id
     * @return a single customer
     */
    public ResponseModel getCustomerById(Long id) {
        Optional<CustomerEntity> customerFound = this.customerDao.findById(id);
        if (customerFound.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("Customer not found with the selected ID");
        } else {
            return new ResponseModel(ResponseCode.C, this.customerMapper.toDTO(customerFound.get()));
        }
    }

    /**
     * @return List of all customers
     */
    public ResponseModel getAllCustomers() {
        List<CustomerDto> customers = this.customerDao.findAll().stream().map(customerMapper::toDTO).toList();
        if (customers.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("No customers were found, the list may be empty");
        } else {
            return new ResponseModel(ResponseCode.E, customers);
        }
    }

    /**
     * @param email String
     * @return a single customer
     */
    public ResponseModel getCustomerByEmail(String email) {
        Optional<CustomerEntity> customerFound = this.customerDao.findCustomerByEmail(email);
        if (customerFound.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("Customer not found with the selected email");
        } else {
            return new ResponseModel(ResponseCode.C, this.customerMapper.toDTO(customerFound.get()));
        }
    }

    /**
     * @param isDeleted Boolean
     * @return all customers with the selected deleted status
     */
    public ResponseModel getCustomerByDeletedStatus(Boolean isDeleted) {
        List<CustomerDto> customers = this.customerDao.findCustomerByIsDeleted(isDeleted).stream().map(customerMapper::toDTO).toList();
        if (customers.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("No customers were found with the selected parameter");
        } else {
            return new ResponseModel(ResponseCode.E, customers);
        }
    }

    /**
     * @param isVerified Boolean
     * @return all customers with the selected verified status
     */
    public ResponseModel getCustomersByVerifiedStatus(Boolean isVerified) {
        List<CustomerDto> customers = this.customerDao.findCustomerByIsVerified(isVerified).stream().map(customerMapper::toDTO).toList();
        if (customers.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("No customers were found with the selected parameter");
        } else {
            return new ResponseModel(ResponseCode.E, customers);
        }
    }

    /**
     * @param id              customer id
     * @param customerUpdates CustomerDto
     * @return a customer updated
     */
    public ResponseModel updateCustomer(Long id, CustomerDto customerUpdates) {
        Optional<CustomerEntity> customerToUpdate = this.customerDao.findById(id);
        if (customerToUpdate.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("Customer not found with the selected ID");
        } else if (customerUpdates != null) {
            if (customerUpdates.getEmail() != null) {
                customerToUpdate.get().setEmail(customerUpdates.getEmail());
            }
            if (customerUpdates.getPassword() != null) {
                customerToUpdate.get().setPassword(customerUpdates.getPassword());
            }
            if (customerUpdates.getIsDeleted() != null) {
                customerToUpdate.get().setIsDeleted(customerUpdates.getIsDeleted());
            }
            if (customerUpdates.getIsVerified() != null) {
                customerToUpdate.get().setIsVerified(customerUpdates.getIsVerified());
            }
            if (customerUpdates.getUserDetails() != null) {
                UserDetailsEntity updatedUserDetails = userDetailsDao.save(customerUpdates.getUserDetails());
                customerToUpdate.get().setUserDetailsEntity(updatedUserDetails);
            }
            return new ResponseModel(ResponseCode.G, customerMapper.toDTO(customerDao.saveAndFlush(customerToUpdate.get())));
        }
        return new ResponseModel(ResponseCode.A).addMessageDetails("Impossible to update, the body should not be null");
    }

    /**
     * @param id          customer id
     * @param customerDto CustomerDto
     * @return customer with password updated
     */
    public ResponseModel updatePassword(Long id, CustomerDto customerDto) {
        Optional<CustomerEntity> customerToUpdate = this.customerDao.findById(id);
        if (customerToUpdate.isEmpty()) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("Customer not found with the selected ID");
        } else if (customerDto != null) {
            if (customerDto.getPassword() != null) {
                customerToUpdate.get().setPassword(customerDto.getPassword());
                return new ResponseModel(ResponseCode.G, customerMapper.toDTO(this.customerDao.saveAndFlush(customerToUpdate.get())));
            }
        }
        return new ResponseModel(ResponseCode.A).addMessageDetails("Impossible to update, the body should not be null");
    }

    /**
     * @param id customer id
     */
    public ResponseModel deleteCustomer(Long id) {
        if (!this.customerDao.existsById(id)) {
            return new ResponseModel(ResponseCode.D).addMessageDetails("Customer not found with the selected ID");
        } else {
            this.customerDao.deleteById(id);
            return new ResponseModel(ResponseCode.H).addMessageDetails("Customer eliminated");
        }
    }

    public ResponseModel deleteAllCustomers() {
        this.customerDao.deleteAll();
        return new ResponseModel(ResponseCode.H).addMessageDetails("All customers eliminated");
    }

}
