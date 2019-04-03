import your.project.po.BasicPo;
import com.github.walker.mybatis.paginator.PageBounds;

import java.util.List;
import java.util.Map;

/**
 * mybatis DAO基类
 */
public interface BasicDao {

    public int insert(BasicPo basicPo);

    public int insertBatch(List list);


    public int update(BasicPo basicPo);

    public int updateIgnoreNull(BasicPo basicPo);

    public int updateBatch(List list);

    public int delete(BasicPo basicPo);

    public int deleteBatch(List list);

    public int deleteById(Long id);

    public int deleteAll();

    public long count();

    public BasicPo findById(Long id);

    public List find(Map<String, Object> paramMap);
}
