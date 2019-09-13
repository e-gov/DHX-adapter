<#ftl output_format="HTML">
<#-- @ftlvariable name="data" type="ee.ria.dhx.HttpMultiPartRequestAttachment" -->
<div><#if data.method??>${data.method}<#else>GET</#if> to <#if data.url??>${data.url}<#else>Unknown</#if></div>


<#if (data.headers)?has_content>
<h4>Headers</h4>
<div>
    <#list data.headers as name, value>
        <div>${name}: ${value}</div>
    </#list>
</div>
</#if>


<#if (data.multiParts)?has_content>

    <#list data.multiParts as item>
        <h4>Multipart ${item?index}</h4>
        <h5>Headers</h5>
        <#list item.headers as name, value>
            <div>${name}: ${value}</div>
        </#list>
        <div>Content-Disposition: form-data; name=${item.controlName}</div>
        <div>Content-Type: ${item.mimeType}; charset=${item.charset}</div>
        <h5>Content</h5>
        <pre class="preformated-text">
${item.content}
        </pre>
    </#list>
</div>
</#if>



