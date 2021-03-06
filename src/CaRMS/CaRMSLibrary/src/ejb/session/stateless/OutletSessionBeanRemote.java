/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Outlet;
import java.util.List;
import util.exception.InputDataValidationException;
import util.exception.OutletNameExistException;
import util.exception.OutletNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author dtjldamien
 */
public interface OutletSessionBeanRemote {

    public Long createNewOutlet(Outlet newOutlet) throws OutletNameExistException, UnknownPersistenceException, InputDataValidationException;

    public Outlet retrieveOutletByOutletId(Long outletId) throws OutletNotFoundException;

    public List<Outlet> retrieveAllOutlets();
    
}
