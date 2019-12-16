import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.time.LocalDateTime

/**
 * Mybatis 代码自动生成脚本
 * V1.1
 */

/**************************************************** 用户自定义 ****************************************************/

/**
 * 实体类包路径
 */
entityClassPackage = "cc.chenzhihao.entity"

/**
 * Mybatis Mapper接口包路径
 */
mapperInterfacePackage = "cc.chenzhihao.mapper"

/**
 * 实体类继承的父类全限定类名
 * 设置为空串，代表实体类不继承父类
 */
entityExtendsSupperClass = "cc.chenzhihao.entity.BaseEntity"

/**
 * 是否自动生成增删改查代码
 */
autoGenerateCRUD = true

/**************************************************** 以下建议不要修改 ****************************************************/

/**
 * 文件输出目录
 */
outDir = PROJECT.getBasePath() + "/generate/"

/**
 * 实体类文件 输出目录
 */
entityOutDir = outDir + "/entity/"

/**
 * Mapper接口类 输出目录
 */
mapperInterfaceOutDir = outDir + "/dao/"

/**
 * MapperXML 输出目录
 */
mapperXMLOutDir = outDir + "/mapper/"

// 当前时间
now = LocalDateTime.now().toString()


/**
 * 数据库字段类型映射
 * 字段类型正则 -> Java字段类型
 */
typeMapping = [
        (~/(?i)int/)                      : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "BigDecimal",
        (~/(?i)smallint|tinyint/)         : "Integer",
        (~/(?i)datetime|timestamp/)       : "java.time.LocalDateTime",
        (~/(?i)date/)                     : "java.time.LocalDate",
        (~/(?i)time/)                     : "java.time.LocalTime",
        (~/(?i)/)                         : "String"
]

/**
 * column 和 jdbcType映射
 * columnType正则 -> jdbcType
 */
columnType2JdbcTypeMap = [
        (~/(?i)CHAR|VARCHAR|TEXT/): "VARCHAR",
        (~/(?i)LONGVARCHAR/)      : "LONGVARCHAR",
        (~/(?i)NUMERIC/)          : "NUMERIC",
        (~/(?i)DECIMAL/)          : "DECIMAL",
        (~/(?i)BIT/)              : "BIT",
        (~/(?i)BOOLEAN/)          : "BOOLEAN",
        (~/(?i)TINYINT/)          : "TINYINT",
        (~/(?i)SMALLINT/)         : "SMALLINT",
        (~/(?i)INTEGER/)          : "INTEGER",
        (~/(?i)INT/)              : "INTEGER",
        (~/(?i)BIGINT/)           : "BIGINT",
        (~/(?i)REAL/)             : "REAL",
        (~/(?i)FLOAT/)            : "FLOAT",
        (~/(?i)DOUBLE/)           : "DOUBLE",
        (~/(?i)BINARY/)           : "BINARY",
        (~/(?i)VARBINARY/)        : "VARBINARY",
        (~/(?i)LONGVARBINARY/)    : "LONGVARBINARY",
        (~/(?i)DATE/)             : "DATE",
        (~/(?i)TIME/)             : "TIME",
        (~/(?i)TIMESTAMP/)        : "TIMESTAMP",
        (~/(?i)CLOB/)             : "CLOB",
        (~/(?i)BLOB/)             : "BLOB",
        (~/(?i)ARRAY/)            : "ARRAY",
        (~/(?i)DISTINCT/)         : "DISTINCT",
        (~/(?i)STRUCT/)           : "STRUCT",
        (~/(?i)REF/)              : "REF",
        (~/(?i)DATALINK/)         : "DATALINK"
]

SELECTION.filter { it instanceof DasTable }.each { generate(it) }

def generate(table) {
    def tableName = table.getName() // 表名
    def tableComment = table.getComment() // 数据库表注释
    def columns = getColumns(table) // 数据库表字段列表
    if (columns == null || columns.size() == 0) {
        throw new Exception("${tableName}表无字段，生成个毛线？？")
    }
    def primaryColumn = columns.find { c -> c.isPrimary } // 主键字段
    if (primaryColumn == null)
        throw new Exception("${tableName}表无主键，必须设置一个主键才可以生成代码")
    def entityClassName = javaName(table.getName(), true) // 类名
    def mapperClassName = entityClassName + "Mapper"
    // 方法参数
    def param = [
            tableName      : tableName,
            tableComment   : tableComment,
            columns        : columns,
            primaryColumn  : primaryColumn,
            entityClassName: entityClassName,
            mapperClassName: mapperClassName,
    ]
    // 生成Entity
    entityDirFile = new File(entityOutDir)
    entityDirFile.mkdirs()
    new File(entityOutDir, String.format("%s.java", entityClassName)).withPrintWriter { out -> generateEntity(out, param) }
    FILES.refresh(entityDirFile)
    // 生成Mapper接口
    mapperInterfateDirFile = new File(mapperInterfaceOutDir)
    mapperInterfateDirFile.mkdirs()
    new File(mapperInterfaceOutDir, String.format("%s.java", mapperClassName)).withPrintWriter { out -> generateMapperInterface(out, param) }
    FILES.refresh(mapperInterfateDirFile)
    // 生成MapperXML
    mapperXMLDirFile = new File(mapperXMLOutDir)
    mapperXMLDirFile.mkdirs()
    new File(mapperXMLOutDir, String.format("%s.xml", mapperClassName)).withPrintWriter { out -> generateMapperXml(out, param) }
    FILES.refresh(mapperXMLDirFile)
}

/**
 * 生成Mybatis Mapper 类文件
 * @param out 文件输出流对象
 * @param param 参数
 */
def generateMapperInterface(out, param) {
    out.println "package $mapperInterfacePackage;"
    out.println ""
    if (autoGenerateCRUD) {
        out.println "import $entityClassPackage.$param.entityClassName;"
    }
    out.println "import org.apache.ibatis.annotations.Mapper;"
    out.println "import org.apache.ibatis.annotations.Param;"
    out.println ""
    out.println "/**"
    if (isNotEmpty(param.tableComment)) {
        out.println " * $param.tableComment 数据库操作接口"
    }
    out.println " * table: $param.tableName"
    out.println " * "
    out.println " * @date $now"
    out.println " */"
    out.println "@Mapper"
    out.println "public interface $param.mapperClassName {"
    out.println ""
    if (autoGenerateCRUD) {
        out.println "\t/**"
        out.println "\t * 新增记录"
        out.println "\t * "
        out.println "\t * @param entity 实体对象"
        out.println "\t * @return 插入记录条数"
        out.println "\t */"
        out.println "\tInteger insert(@Param(\"entity\") $param.entityClassName entity);"
        out.println ""
        out.println "\t/**"
        out.println "\t * 根据主键删除记录"
        out.println "\t * "
        out.println "\t * @param $param.primaryColumn.propertyName 主键"
        out.println "\t * @return 删除记录行数"
        out.println "\t */"
        out.println "\tInteger deleteByPrimaryKey(@Param(\"$param.primaryColumn.propertyName\") $param.primaryColumn.javaType $param.primaryColumn.propertyName);"
        out.println ""
        out.println "\t/**"
        out.println "\t * 根据主键修改记录"
        out.println "\t * "
        out.println "\t * @param entity 实体对象"
        out.println "\t * @return 修改记录行数"
        out.println "\t */"
        out.println "\tInteger updateByPrimaryKey(@Param(\"entity\") $param.entityClassName entity);"
        out.println ""
        out.println "\t/**"
        out.println "\t * 根据主键查找单条记录"
        out.println "\t * "
        out.println "\t * @param $param.primaryColumn.propertyName 主键"
        out.println "\t * @return 实体对象"
        out.println "\t */"
        out.println "\t$param.entityClassName selectByPrimaryKey(@Param(\"$param.primaryColumn.propertyName\") $param.primaryColumn.javaType $param.primaryColumn.propertyName);"
        out.println ""
    }
    out.println "}"
}

/**
 * 生成Mybatis Mapper xml文件
 * @param out 文件输出流对象
 * @param param 参数
 */
def generateMapperXml(out, param) {
    out.println "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    out.println "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >"
    out.println "<mapper namespace=\"$mapperInterfacePackage.$param.mapperClassName\">"
    out.println ""
    out.println "\t<!-- 基础实体ResultMap -->"
    out.println "\t<resultMap id=\"BaseResultMap\" type=\"$entityClassPackage.$param.entityClassName\">"
    param.columns.each() {
        out.println "\t\t<result column=\"${it.columnName}\" property=\"${it.propertyName}\" jdbcType=\"${it.jdbcType}\"/>"
    }
    out.println "\t</resultMap>"
    out.println ""
    out.println "\t<!-- 所有字段 -->"
    out.println "\t<sql id=\"Base_Column_List\">"
    out.print "\t\t"
    param.columns.eachWithIndex { it, index ->
        out.print "`${it.columnName}`"
        if (index < param.columns.size() - 1) {
            out.print ", "
        }
    }
    out.println ""
    out.println "\t</sql>"
    out.println ""
    if (autoGenerateCRUD) {
        out.println "\t<insert id=\"insert\">"
        out.println "\t\tINSERT INTO `$param.tableName` ("
        out.print "\t\t\t"
        param.columns.eachWithIndex { it, index ->
            if (it.isPrimary) return
            out.print "`${it.columnName}`"
            if (index < param.columns.size() - 1) {
                out.print ", "
            }
        }
        out.println "\n\t\t) VALUES ("
        out.print "\t\t\t"
        param.columns.eachWithIndex { it, index ->
            if (it.isPrimary) return
            out.print "#{entity.${it.propertyName}}"
            if (index < param.columns.size() - 1) {
                out.print ", "
            }
        }
        out.println "\n\t\t)"
        out.println "\t</insert>"
        out.println ""
        out.println "\t<delete id=\"deleteByPrimaryKey\">"
        out.println "\t\tDELETE FROM `$param.tableName` WHERE `$param.primaryColumn.columnName` = #{$param.primaryColumn.propertyName}"
        out.println "\t</delete>"
        out.println ""
        out.println "\t<update id=\"updateByPrimaryKey\">"
        out.println "\t\tUPDATE `$param.tableName` SET"
        param.columns.eachWithIndex { it, index ->
            if (it.isPrimary) return
            out.print "\t\t`${it.columnName}` = #{entity.${it.propertyName}}"
            if (index < param.columns.size() - 1) {
                out.print ",\n"
            }
        }
        out.println "\n\t\tWHERE `$param.primaryColumn.columnName` = #{$param.primaryColumn.propertyName}"
        out.println "\t</update>"
        out.println ""
        out.println "\t<select id=\"selectByPrimaryKey\" resultMap=\"BaseResultMap\">"
        out.println "\t\tSELECT"
        out.println "\t\t<include refid=\"Base_Column_List\"/>"
        out.println "\t\tFROM `$param.tableName`"
        out.println "\t\tWHERE `$param.primaryColumn.columnName` = #{$param.primaryColumn.propertyName}"
        out.println "</select>"
        out.println ""
    }
    out.println "</mapper>"
}

/**
 * 生成数据库实体
 * @param out 文件输出流对象
 * @param param 参数
 */
def generateEntity(out, param) {
    out.println "package $entityClassPackage;"
    out.println ""
    if (isNotEmpty(entityExtendsSupperClass)) {
        out.println "import $entityExtendsSupperClass;"
        out.println "import lombok.EqualsAndHashCode;"
        out.println "import lombok.ToString;"
    }
    out.println "import lombok.Data;"
    out.println ""
    out.println "/**"
    if (isNotEmpty(param.tableComment)) {
        out.println " * $param.tableComment"
    }
    out.println " * table: $param.tableName"
    out.println " * "
    out.println " * @date $now"
    out.println " */"
    if (isNotEmpty(entityExtendsSupperClass)) {
        out.println "@EqualsAndHashCode(callSuper = true)"
        out.println "@ToString(callSuper = true)"
    }
    out.println "@Data"
    out.print "public class $param.entityClassName "
    if (isNotEmpty(entityExtendsSupperClass)) {
        def supperClassName = entityExtendsSupperClass.substring(entityExtendsSupperClass.lastIndexOf(".") + 1)
        out.print "extends $supperClassName"
    }
    out.println "{\n"
    param.columns.each() {
        out.println "    /**"
        if (isNotEmpty(it.columnComment))
            out.println "     * ${it.columnComment}"
        out.println "     * 字段名：${it.columnName}"
        if (it.isPrimary)
            out.println "     * 主键"
        out.println "     */"
        out.println "\tprivate ${it.javaType} ${it.propertyName};"
        out.println ""
    }
    out.println "}"
}


/******************************工具方法*****************************/
/**
 * 获取数据库表字段集合
 */
def getColumns(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def columnTypeSpecification = col.getDataType().getSpecification()
        def columnType = col.getDataType().typeName
        def spec = Case.LOWER.apply(columnTypeSpecification)
        def propertyName = javaName(col.getName(), false)
        def propertyType = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        def jdbcType = columnType2JdbcTypeMap.find { p, t -> p.matcher(columnType).find() }.value
        fields += [
                [
                        columnName   : col.getName(), // 数据库字段名称
                        jdbcType     : jdbcType, // 数据库字段类型
                        columnComment: col.getComment(), // 数据库字段注释
                        propertyName : propertyName, // java属性名
                        javaType     : propertyType, // java属性类型
                        isPrimary    : DasUtil.isPrimary(col), // 是否是主键
                ]
        ]
    }
}

// 获取获取驼峰类名
def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}


def isNotEmpty(content) {
    return content != null && content.toString().trim().length() > 0
}
