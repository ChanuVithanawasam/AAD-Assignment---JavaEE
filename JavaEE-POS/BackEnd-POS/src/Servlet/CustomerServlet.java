package Servlet;

import bo.BOFactory;
import bo.custom.CustomerBO;
import bo.custom.QueryBO;
import dto.CustomerDTO;
import util.CrudUtil;

import javax.annotation.Resource;
import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getBoFactory().getBO(BOFactory.BOTypes.CUSTOMER);
    private final QueryBO queryBO = (QueryBO) BOFactory.getBoFactory().getBO(BOFactory.BOTypes.CUSTOM);

    @Resource(name = "java:comp/env/jdbc/pool")
    DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonArrayBuilder allCustomers = Json.createArrayBuilder();

        String id = req.getParameter("id");
        String option = req.getParameter("option");

        PrintWriter writer = resp.getWriter();
        switch (option) {
            case "searchCusId":
                try (Connection connection = dataSource.getConnection()) {
                    ArrayList<CustomerDTO> arrayList = customerBO.customerSearchId(id, connection);

                    if (arrayList.isEmpty()) {
                        JsonObjectBuilder rjo = Json.createObjectBuilder();
                        rjo.add("state", "Error");
                        rjo.add("message", "Customer");
                        rjo.add("data", "");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().print(rjo.build());

                    } else {
                        for (CustomerDTO customerDTO : arrayList) {
                            JsonObjectBuilder customer = Json.createObjectBuilder();
                            customer.add("id", customerDTO.getId());
                            customer.add("name", customerDTO.getName());
                            customer.add("address", customerDTO.getAddress());
                            customer.add("salary", customerDTO.getSalary());
                            writer.print(customer.build());
                        }
                    }

                } catch (SQLException | ClassNotFoundException e) {

                    JsonObjectBuilder rjo = Json.createObjectBuilder();
                    rjo.add("state", "Error");
                    rjo.add("message", e.getLocalizedMessage());
                    rjo.add("data", "");
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print(rjo.build());
                }

                break;
            case "loadAllCustomer":
                try (Connection connection = dataSource.getConnection()) {
                    ArrayList<CustomerDTO> obList = customerBO.getAllCustomers(connection);

                    for (CustomerDTO customerDTO : obList) {
                        JsonObjectBuilder customer = Json.createObjectBuilder();
                        customer.add("id", customerDTO.getId());
                        customer.add("name", customerDTO.getName());
                        customer.add("address", customerDTO.getAddress());
                        customer.add("salary", customerDTO.getSalary());
                        allCustomers.add(customer.build());
                    }

                    JsonObjectBuilder job = Json.createObjectBuilder();
                    job.add("state", "Ok");
                    job.add("message", "Successfully Loaded..!");
                    job.add("data", allCustomers.build());
                    resp.getWriter().print(job.build());

                } catch (ClassNotFoundException | SQLException e) {
                    JsonObjectBuilder rjo = Json.createObjectBuilder();
                    rjo.add("state", "Error");
                    rjo.add("message", e.getLocalizedMessage());
                    rjo.add("data", "");
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print(rjo.build());
                }
                break;
            case "CustomerIdGenerate":
                try (Connection connection = dataSource.getConnection()) {
                    String cID = customerBO.generateNewCustomerID(connection);

                    JsonObjectBuilder CusId = Json.createObjectBuilder();
                    CusId.add("id", cID);
                    writer.print(CusId.build());

                } catch (SQLException | ClassNotFoundException e) {

                    JsonObjectBuilder rjo = Json.createObjectBuilder();
                    rjo.add("state", "Error");
                    rjo.add("message", e.getLocalizedMessage());
                    rjo.add("data", "");
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print(rjo.build());
                }
                break;
            case "CustomerCount":
                try (Connection connection = dataSource.getConnection()) {
                    int countCustomers = queryBO.getCustomer(connection);

                    JsonObjectBuilder count = Json.createObjectBuilder();
                    count.add("count", countCustomers);
                    writer.print(count.build());

                } catch (SQLException | ClassNotFoundException e) {

                    JsonObjectBuilder rjo = Json.createObjectBuilder();
                    rjo.add("state", "Error");
                    rjo.add("message", e.getLocalizedMessage());
                    rjo.add("data", "");
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print(rjo.build());
                }
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String address = req.getParameter("address");
        double salary = Double.parseDouble(req.getParameter("salary"));

        try (Connection connection = dataSource.getConnection()) {
            CustomerDTO c = new CustomerDTO(id, name, address, salary);
            boolean b = customerBO.saveCustomer(c, connection);

            if (b) {
                JsonObjectBuilder responseObject = Json.createObjectBuilder();
                responseObject.add("state", "Ok");
                responseObject.add("message", "Successfully added..!");
                responseObject.add("data", "");
                resp.getWriter().print(responseObject.build());

            }
        } catch (SQLException e) {

            JsonObjectBuilder error = Json.createObjectBuilder();
            error.add("state", "Error");
            error.add("message", e.getLocalizedMessage());
            error.add("data", "");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(error.build());

        } catch (ClassNotFoundException e) {

            JsonObjectBuilder error = Json.createObjectBuilder();
            error.add("state", "Error");
            error.add("message", e.getLocalizedMessage());
            error.add("data", "");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(error.build());

        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customer = reader.readObject();

        String id = customer.getString("id");
        String name = customer.getString("name");
        String address = customer.getString("address");
        double salary = Double.parseDouble(customer.getString("salary"));

        //Update Customer
        CustomerDTO cU = new CustomerDTO(id, name, address, salary);
        try (Connection connection = dataSource.getConnection()) {
            boolean b = customerBO.updateCustomer(cU, connection);
            if (b) {
                JsonObjectBuilder responseObject = Json.createObjectBuilder();
                responseObject.add("state", "Ok");
                responseObject.add("message", "Successfully Updated..!");
                responseObject.add("data", "");
                resp.getWriter().print(responseObject.build());

            } else {
                throw new RuntimeException("Wrong ID, Please Check The ID..!");
            }

        } catch (RuntimeException e) {

            JsonObjectBuilder rjo = Json.createObjectBuilder();
            rjo.add("state", "Error");
            rjo.add("message", e.getLocalizedMessage());
            rjo.add("data", "");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(rjo.build());

        } catch (ClassNotFoundException | SQLException e) {

            JsonObjectBuilder rjo = Json.createObjectBuilder();
            rjo.add("state", "Error");
            rjo.add("message", e.getLocalizedMessage());
            rjo.add("data", "");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(rjo.build());

        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customer = reader.readObject();

        String id = customer.getString("id");

        //Delete Customer
        try (Connection connection = dataSource.getConnection()) {
            boolean b = customerBO.deleteCustomer(id, connection);
            if (b) {
                JsonObjectBuilder rjo = Json.createObjectBuilder();
                rjo.add("state", "Ok");
                rjo.add("message", "Successfully Deleted..!");
                rjo.add("data", "");
                resp.getWriter().print(rjo.build());

            } else {
                throw new RuntimeException("There is no such customer for that ID..!");
            }
        } catch (RuntimeException e) {

            JsonObjectBuilder rjo = Json.createObjectBuilder();
            rjo.add("state", "Error");
            rjo.add("message", e.getLocalizedMessage());
            rjo.add("data", "");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(rjo.build());

        } catch (ClassNotFoundException | SQLException e) {

            JsonObjectBuilder rjo = Json.createObjectBuilder();
            rjo.add("state", "Error");
            rjo.add("message", e.getLocalizedMessage());
            rjo.add("data", "");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(rjo.build());

        }
    }

}

