package io.github.ptitjes.sqldelight.vec

import app.cash.sqldelight.dialect.api.DialectType
import app.cash.sqldelight.dialect.api.IntermediateType
import app.cash.sqldelight.dialect.api.PrimitiveType.BLOB
import app.cash.sqldelight.dialect.api.PrimitiveType.INTEGER
import app.cash.sqldelight.dialect.api.PrimitiveType.REAL
import app.cash.sqldelight.dialect.api.PrimitiveType.TEXT
import app.cash.sqldelight.dialect.api.SqlDelightModule
import app.cash.sqldelight.dialect.api.TypeResolver
import com.alecstrong.sql.psi.core.psi.SqlExpr
import com.alecstrong.sql.psi.core.psi.SqlFunctionExpr
import com.intellij.psi.PsiElement
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName


class SqliteVecModule : SqlDelightModule {
    override fun typeResolver(parentResolver: TypeResolver): TypeResolver = SqliteVecTypeResolver(parentResolver)

    override fun setup() {
        // JsonParserUtil.reset()
        // JsonParserUtil.overrideSqlParser()
    }
}

private class SqliteVecTypeResolver(private val parentResolver: TypeResolver) : TypeResolver by parentResolver {

    override fun argumentType(parent: PsiElement, argument: SqlExpr): IntermediateType {
        return (parent as? SqlFunctionExpr)?.sqliteVecFunctionArgumentType()
            ?: parentResolver.argumentType(parent, argument)
    }

    private fun SqlFunctionExpr.sqliteVecFunctionArgumentType() = when (functionName.text.lowercase()) {
        "vec_f32" -> IntermediateType(VecType.F32)
        "vec_int8" -> IntermediateType(VecType.INT8)
        "vec_add", "vec_sub", "vec_normalize", "vec_length", "vec_distance_cosine", "vec_distance_L2",
        "vec_type", "vec_to_json",
            -> IntermediateType(BLOB)

        else -> null
    }

    override fun functionType(functionExpr: SqlFunctionExpr): IntermediateType? {
        return functionExpr.sqliteVecFunctionType() ?: parentResolver.functionType(functionExpr)
    }

    private fun SqlFunctionExpr.sqliteVecFunctionType() = when (functionName.text.lowercase()) {
        "vec_f32", "vec_int8", "vec_add", "vec_sub", "vec_normalize", "vec_slice" -> IntermediateType(BLOB)
        "vec_length" -> IntermediateType(INTEGER)
        "vec_distance_cosine", "vec_distance_L2" -> IntermediateType(REAL)
        "vec_type", "vec_to_json" -> IntermediateType(TEXT)
        else -> null
    }
}

enum class VecType(elementType: TypeName) : DialectType {
    F32(DOUBLE),
    INT8(BYTE);

    override val javaType: TypeName = LIST.parameterizedBy(elementType)

    override fun prepareStatementBinder(columnIndex: CodeBlock, value: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .add("bindString(%L, (%L).joinToString(\",\", \"[\", \"]\") { it.toString() })\n", columnIndex, value)
            .build()
    }

    override fun cursorGetter(columnIndex: Int, cursorName: String): CodeBlock {
        return CodeBlock.of(
            when (this) {
                F32 -> "$cursorName.getString($columnIndex).removePrefix(\"[\").removeSuffix(\"]\").split(\",\").map { it.toDouble() }"
                INT8 -> "$cursorName.getString($columnIndex).removePrefix(\"[\").removeSuffix(\"]\").split(\",\").map { it.toByte() }"
            },
        )
    }
}
